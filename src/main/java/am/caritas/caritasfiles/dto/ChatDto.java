package am.caritas.caritasfiles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDto {
    private Long id;
    private String content;
    private String user;
    private String discussion;
}
