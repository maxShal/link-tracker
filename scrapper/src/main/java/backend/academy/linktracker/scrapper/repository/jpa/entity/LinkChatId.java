    package backend.academy.linktracker.scrapper.repository.jpa.entity;

    import jakarta.persistence.Embeddable;
    import lombok.AllArgsConstructor;
    import lombok.EqualsAndHashCode;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import java.io.Serializable;

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    public class LinkChatId implements Serializable {
        private Long chatId;
        private Long linkId;
    }
