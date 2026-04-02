package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chats")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ChatsEntity {

    @Id
    private Long id;
}
