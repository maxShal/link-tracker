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

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "link_tag")
public class LinkTagEntity {
    @EmbeddedId
    private LinkTagId id;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private LinksEntity linksEntity;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private TagsEntity tagsEntity;
}
