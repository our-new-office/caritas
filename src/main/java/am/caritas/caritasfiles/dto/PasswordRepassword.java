package am.caritas.caritasfiles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordRepassword {
    private String password;
    private String rePassword;
    private Long userId;
    private String token;

}
