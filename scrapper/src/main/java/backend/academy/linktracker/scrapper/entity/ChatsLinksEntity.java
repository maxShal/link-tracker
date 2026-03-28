package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Table(name = "link_chat")
@Entity
@Getter
@Setter
@AllArgsConstructor
public class ChatsLinksEntity {
    @EmbeddedId
    private LinkChatId id;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private ChatsEntity chatsEntity;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private LinksEntity linksEntity;
}
