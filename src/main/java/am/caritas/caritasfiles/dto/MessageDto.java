package am.caritas.caritasfiles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDto {
    private long userId;
    private long discussionId;
    private String text;
    private long chatId;
    private String file;
    private String date;
}