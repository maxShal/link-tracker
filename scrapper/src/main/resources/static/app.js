'use strict';
const $ = (id) => document.getElementById(id);
const state = { chat: '', page: 0, size: 5, count: 0, busy: false, deleting: null };
const node = (tag, className, text) => { const el = document.createElement(tag); el.className = className; if (text !== undefined) el.textContent = text; return el; };
function status(message, error = false) { $('status').textContent = message; $('status').classList.toggle('error', error); }
function controls() {
  document.querySelectorAll('button').forEach((button) => { button.disabled = state.busy; });
  $('chat-id').disabled = state.busy || !!state.chat;
  document.querySelector('#chat-form button[type="submit"]').hidden = !!state.chat;
  $('previous').disabled = state.busy || state.page === 0;
  $('next').disabled = state.busy || state.count < state.size;
}
async function action(fn) {
  if (state.busy) return;
  state.busy = true; controls();
  try { await fn(); } catch (error) { status(error.message || 'Не удалось выполнить действие.', true); }
  finally { state.busy = false; controls(); }
}
async function api(path, method = 'GET', body, chat = state.chat) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 15000);
  try {
    const response = await fetch(path, { method, headers: { 'Tg-Chat-Id': chat, ...(body ? { 'Content-Type': 'application/json' } : {}) }, body: body ? JSON.stringify(body) : undefined, signal: controller.signal });
    const text = await response.text();
    let data; try { data = text ? JSON.parse(text) : null; } catch { data = null; }
    if (!response.ok) {
      const messages = { 400: 'Проверьте ссылку, теги и Chat ID.', 404: 'Чат, ссылка или тег не найдены. Обновите список.', 409: 'Такая запись уже существует.', 429: 'Слишком много запросов. Подождите несколько секунд и повторите.' };
      throw new Error(messages[response.status] || `Сервер не выполнил запрос (HTTP ${response.status}). Повторите позже.`);
    }
    return data;
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('Сервер не ответил за 15 секунд. Повторите запрос.');
    if (error instanceof TypeError) throw new Error('Нет соединения с сервером. Проверьте, запущен ли Scrapper.');
    throw error;
  } finally { clearTimeout(timer); }
}
function safeUrl(raw) { try { const url = new URL(raw); return ['http:', 'https:'].includes(url.protocol) ? url : null; } catch { return null; } }
function splitTags(value) { return [...new Set(value.split(',').map((tag) => tag.trim()).filter(Boolean))]; }
function render(links) {
  $('links').replaceChildren();
  $('page-number').textContent = `Страница ${state.page + 1}`;
  $('page-info').textContent = `Чат ${state.chat} · На странице: ${links.length}`;
  if (!links.length) {
    const empty = node('div', 'empty');
    empty.append(node('strong', '', state.page ? 'Больше ссылок нет' : 'Начните с первой ссылки'), node('p', '', state.page ? 'Вернитесь на предыдущую страницу.' : 'Добавьте репозиторий GitHub или вопрос Stack Overflow через форму.'));
    $('links').append(empty);
  }
  for (const link of links) {
    const card = node('article', 'link-card');
    const top = node('div', 'link-top');
    const parsed = safeUrl(link.url);
    const github = parsed?.hostname === 'github.com';
    const main = node('div', 'link-main');
    const anchor = node(parsed ? 'a' : 'span', '', parsed ? parsed.pathname.replace(/^\//, '') || parsed.hostname : link.url);
    if (parsed) { anchor.href = parsed.href; anchor.target = '_blank'; anchor.rel = 'noopener noreferrer'; }
    main.append(anchor, node('p', 'url', link.url));
    const remove = node('button', 'remove-link', 'Удалить'); remove.type = 'button';
    remove.setAttribute('aria-label', `Удалить подписку ${link.url}`);
    remove.onclick = () => { state.deleting = link.url; $('delete-url').textContent = link.url; $('delete-dialog').showModal(); };
    top.append(node('span', 'site-icon', github ? 'GH' : 'SO'), main, remove); card.append(top);
    const tags = node('div', 'tags');
    for (const tag of link.tags || []) {
      const chip = node('span', 'tag'); chip.append(node('span', '', tag));
      const removeTag = node('button', '', '×'); removeTag.type = 'button'; removeTag.setAttribute('aria-label', `Удалить тег ${tag}`);
      removeTag.onclick = () => action(async () => { await api('/tags', 'DELETE', { url: link.url, tag }); await loadTags(link, tags); status('Тег удалён.'); });
      chip.append(removeTag); tags.append(chip);
    }
    card.append(tags);
    const form = node('form', 'tag-form'); const input = node('input', ''); input.placeholder = 'Добавить теги через запятую'; input.setAttribute('aria-label', `Новые теги для ${link.url}`); input.required = true;
    const add = node('button', 'secondary', '+ Теги'); add.type = 'submit'; form.append(input, add);
    form.onsubmit = (event) => { event.preventDefault(); action(async () => { const values = splitTags(input.value); if (!values.length) throw new Error('Введите хотя бы один тег.'); await api('/tags', 'POST', { url: link.url, tags: values }); await loadTags(link, tags); input.value = ''; status('Теги добавлены.'); }); };
    card.append(form); $('links').append(card);
  }
}
async function loadTags(link, container) {
  const data = await api(`/tags?url=${encodeURIComponent(link.url)}`);
  link.tags = data.tags || [];
  container.replaceChildren();
  for (const tag of link.tags) {
    const chip = node('span', 'tag'); chip.append(node('span', '', tag));
    const button = node('button', '', '×'); button.type = 'button'; button.setAttribute('aria-label', `Удалить тег ${tag}`);
    button.onclick = () => action(async () => { await api('/tags', 'DELETE', { url: link.url, tag }); await loadTags(link, container); status('Тег удалён.'); });
    chip.append(button); container.append(chip);
  }
}
async function loadPage(page = state.page) {
  status('Загружаем ссылки…');
  const data = await api(`/links?page=${page}&size=${state.size}`);
  if (!data || !Array.isArray(data.links)) throw new Error('Сервер вернул неожиданный ответ.');
  state.page = page; state.count = data.links.length; render(data.links);
  status('Список обновлён. Уведомления о событиях приходят в Telegram.');
}
$('chat-form').onsubmit = (event) => { event.preventDefault(); action(async () => {
  const chat = $('chat-id').value.trim();
  if (!/^-?\d+$/.test(chat) || BigInt(chat) < -9223372036854775808n || BigInt(chat) > 9223372036854775807n) throw new Error('Введите корректный числовой Chat ID.');
  status('Проверяем чат…');
  if (await api(`/tg-chat/${encodeURIComponent(chat)}`, 'GET', undefined, chat) !== true) throw new Error('Чат не зарегистрирован. Отправьте боту /start и проверьте ID.');
  state.chat = chat;
  try { await loadPage(0); $('workspace').hidden = false; $('disconnect').hidden = false; } catch (error) { state.chat = ''; throw error; }
}); };
$('disconnect').onclick = () => { state.chat = ''; state.page = 0; $('workspace').hidden = true; $('disconnect').hidden = true; $('links').replaceChildren(); controls(); $('chat-id').focus(); status('Введите Chat ID, чтобы загрузить подписки.'); };
$('add-form').onsubmit = (event) => { event.preventDefault(); action(async () => {
  const raw = $('link-url').value.trim(); const url = safeUrl(raw);
  const supported = url && url.protocol === 'https:' && ((url.hostname === 'github.com' && /^\/[^/]+\/[^/]+\/?$/.test(url.pathname)) || (['stackoverflow.com', 'ru.stackoverflow.com'].includes(url.hostname) && /^\/questions\/\d+/.test(url.pathname)));
  if (!supported || url.search || url.hash) throw new Error('Укажите HTTPS-ссылку на репозиторий github.com/owner/repo или вопрос Stack Overflow, без параметров и фрагмента.');
  await api('/links', 'POST', { link: raw.replace(/\/$/, ''), tags: splitTags($('link-tags').value) });
  $('add-form').reset(); await loadPage(0); status('Ссылка добавлена. Она также доступна в Telegram.');
}); };
$('refresh').onclick = () => action(() => loadPage());
$('previous').onclick = () => action(() => loadPage(Math.max(0, state.page - 1)));
$('next').onclick = () => action(() => loadPage(state.page + 1));
$('cancel-delete').onclick = () => $('delete-dialog').close();
$('confirm-delete').onclick = () => action(async () => {
  const url = state.deleting; $('delete-dialog').close();
  await api('/links', 'DELETE', { link: url });
  await loadPage(state.count === 1 && state.page > 0 ? state.page - 1 : state.page); status('Подписка удалена.');
});
controls();
