package am.caritas.caritasfiles.model.mail;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Mail {
    private String from;
    private String to;
    private String subject;
    private String content;
}
