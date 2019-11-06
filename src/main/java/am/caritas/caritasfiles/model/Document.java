package am.caritas.caritasfiles.model;

import am.caritas.caritasfiles.model.enums.FileStatus;
import am.caritas.caritasfiles.model.enums.FileType;
import am.caritas.caritasfiles.model.enums.Status;
import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Builder
@Table(name = "document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String url;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    private FileStatus fileStatus;
}
