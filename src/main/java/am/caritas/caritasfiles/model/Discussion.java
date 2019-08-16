package am.caritas.caritasfiles.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "discussion")
public class Discussion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String thumbnail;

    @ManyToMany
    private List<Document> documents;

    @ManyToMany
    private List<Link> links;

    @ManyToMany
    private List<Chat> chats;

    @ManyToMany
    private List<User> users;

    @ManyToOne
    private WorkingGroup workingGroup;
}
