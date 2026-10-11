mini# Запуск LinkTracker в minikube

Команды выполняются из корня проекта в PowerShell. Манифесты используют namespace
`linktracker` и образы `linktracker-bot:hw2`, `linktracker-scrapper:hw2`.
Kafka, Valkey и ai-agent для основного HTTP-варианта не нужны.

## 1. Кластер и образы

Запустите Docker Desktop с Linux-контейнерами:

```powershell
minikube start --driver=docker --container-runtime=docker
kubectl config use-context minikube
minikube image ls
```

Если оба образа hw2 уже в списке, повторная сборка не нужна.
Если меняли исходники (включая UI), пересоберите JAR и образы:

```powershell
.\mvnw.cmd -pl bot,scrapper -am clean package -DskipTests
minikube docker-env --shell powershell | Invoke-Expression
$Env:DOCKER_CERT_PATH = "$HOME\.minikube\certs"
docker build -t linktracker-bot:hw2 ./bot
docker build -t linktracker-scrapper:hw2 ./scrapper
minikube image ls
```

Исправление DOCKER_CERT_PATH выполняется ПОСЛЕ docker-env из-за кириллицы в пути.
При сборке через docker-env образы уже в кластере. Если собирали Docker Desktop,
загрузите их командами `minikube image load linktracker-bot:hw2` и
`minikube image load linktracker-scrapper:hw2`.
После пересборки того же тега перезапустите Deployment через rollout restart.

## 2. Конфигурация и запуск

```powershell
Copy-Item k8s/secrets.yaml.example k8s/secrets.local.yaml
```

Отредактируйте secrets.local.yaml: замените пароль и токены.
Этот файл исключён из Git. PostgreSQL создаёт БД linktracker при первом запуске.
Существующий каталог Compose pgdata автоматически не переносится.
Изменение Secret не меняет пароль уже инициализированной БД.

```powershell
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/
kubectl rollout status statefulset/postgres -n linktracker --timeout=300s
kubectl rollout status deployment/scrapper -n linktracker --timeout=300s
kubectl rollout status deployment/bot -n linktracker --timeout=300s
kubectl get pods,svc,pvc -n linktracker
```

kubectl apply -f k8s/ читает YAML-файлы, включая secrets.local.yaml;
шаблон с расширением .example не применяется.
Проверки TCP подтверждают открытие порта, но не проверяют внешние API.
Миграции Liquibase запускаются внутри scrapper.

## 3. UI

```powershell
kubectl port-forward service/scrapper 8081:8081 -n linktracker
```

Оставьте терминал открытым. Откройте http://localhost:8081.
Отправьте /start боту, введите ID зарегистрированного чата в UI.
Проверьте просмотр подписок, добавление ссылки и тегов, удаление ссылки.
Остановите другие экземпляры того же Telegram-бота вне Kubernetes, если они работают.

## 4. Демонстрация масштабирования

В другом терминале:

```powershell
kubectl scale deployment scrapper --replicas=3 -n linktracker
kubectl rollout status deployment/scrapper -n linktracker --timeout=300s
kubectl get pods -n linktracker -o wide
```

Каждая реплика scrapper запускает собственный планировщик проверки ссылок.
Это демонстрация масштабирования подов, а не гарантия однократной доставки
уведомлений. Для полноценной работы нескольких реплик нужен общий механизм
блокировки заданий или отдельный единственный процесс проверки.
Bot оставлен в одной реплике из-за Telegram polling и диалогов в памяти.
PostgreSQL также остаётся в одной реплике: увеличение replicas не создаёт
репликацию базы данных.

После демонстрации:

```powershell
kubectl scale deployment scrapper --replicas=1 -n linktracker
```

## 5. Диагностика

```powershell
kubectl get pods -n linktracker
kubectl get events -n linktracker --sort-by=.metadata.creationTimestamp
kubectl logs deployment/scrapper -n linktracker --tail=100
kubectl logs deployment/bot -n linktracker --tail=100
kubectl logs postgres-0 -n linktracker --tail=100
```

Для проблемного пода используйте kubectl describe pod ИМЯ -n linktracker.
Если PVC Pending, проверьте kubectl get storageclass: нужен класс standard.
Если ImagePullBackOff, проверьте имя образа и minikube image ls.
Для PostgreSQL и initContainers нужен доступ к registry для postgres:18.6.
При нехватке памяти увеличьте ресурсы Docker Desktop/minikube.

## 6. Сдача

Запишите до 5–7 минут: kubectl apply, вывод готовых подов, kubectl scale,
вывод трёх реплик scrapper, несколько действий в UI.
Перед развёртыванием выполните упражнения kubectl из семинарского архива.
В архив решения включите Dockerfile bot и scrapper, манифесты, этот README,
файл Отчёт.md из данного каталога и скринкаст.
Включайте шаблон Secret с заглушками вместо secrets.local.yaml с настоящими токенами.
Образы и запущенное приложение требуют отдельной проверки; наличие YAML не
подтверждает успешный запуск.

## Как устроены инструменты

Docker собирает образ из Dockerfile и JAR. Docker Desktop запускает контейнер
узла minikube. Docker runtime внутри этого узла запускает контейнеры подов.
minikube создаёт, запускает и останавливает локальный Kubernetes-кластер,
а docker-env направляет Docker CLI к его внутреннему демону.
kubectl обращается к Kubernetes API выбранного кластера: apply передаёт
желаемое состояние, контроллеры создают поды, scale меняет число реплик.
minikube и kubectl используются совместно и решают разные задачи.
