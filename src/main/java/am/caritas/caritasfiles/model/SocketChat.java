package am.caritas.caritasfiles.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class SocketChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TEXT")
    private String text;

    @ManyToOne
    private User sender;

    @ManyToOne
    private Discussion discussion;

    @Column
    private Date date;

    @Column
    private String file;

}

