package am.caritas.caritasfiles.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Builder
@Table(name = "user_discussion_files")
public class UserDiscussionFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private Date date;

    @ManyToOne
    private Discussion discussion;

    @Column
    private String url;

}
