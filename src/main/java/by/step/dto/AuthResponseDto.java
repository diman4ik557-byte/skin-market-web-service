package by.step.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponseDto {
    private String token;
    private String type;
    private String message;
    private String username;
    private String email;
    private String role;

    @JsonCreator
    public AuthResponseDto(
            @JsonProperty("token") String token,
            @JsonProperty("type") String type) {
        this.token = token;
        this.type = type;
    }
}