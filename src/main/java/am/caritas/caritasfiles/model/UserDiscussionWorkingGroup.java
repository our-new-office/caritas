package am.caritas.caritasfiles.model;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name="user_discussion_working_group")
public class UserDiscussionWorkingGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @OneToMany
    private User user;

    @OneToMany
    private Discussion discussion;

    @OneToMany
    private WorkingGroup workingGroup;
}
