package am.caritas.caritasfiles.dto;

import am.caritas.caritasfiles.model.WorkingGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkingGroupDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private Long userId;

}
