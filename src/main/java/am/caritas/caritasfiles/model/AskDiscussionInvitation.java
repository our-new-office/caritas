package am.caritas.caritasfiles.model;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@Entity
@Table(name = "ask_discussion_invitation")
public class AskDiscussionInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Discussion discussion;

    @Column
    private Boolean hasSent;

}
