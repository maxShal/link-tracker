package backend.academy.linktracker.scrapper.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(name = "links")
@Getter
@Setter
@AllArgsConstructor
public class LinksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String url;
    /*
    @ElementCollection
    @CollectionTable(
        name = "link_tags",
        joinColumns = @JoinColumn(name = "link_id")
    )*/
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    private OffsetDateTime lastUpdatedAt;
}
