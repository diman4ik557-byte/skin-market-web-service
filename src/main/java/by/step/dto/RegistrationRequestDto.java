package by.step.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для регистрации нового пользователя.
 * Содержит данные, необходимые для создания учетной записи.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDto {

    /**
     * Имя пользователя (логин).
     * Должно быть от 3 до 50 символов.
     */
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Имя пользователя может содержать только буквы, цифры и подчеркивание")
    private String username;

    /**
     * Пароль пользователя.
     * Должен быть от 4 до 100 символов.
     */
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 4, max = 100, message = "Пароль должен быть от 4 до 100 символов")
    private String password;

    /**
     * Email адрес пользователя.
     * Должен соответствовать формату email.
     */
    @NotBlank(message = "Email обязателен")
    @Email(message = "Введите корректный email адрес")
    private String email;

    /**
     * Роль пользователя в системе.
     * По умолчанию - USER.
     */
    private String role = "USER";
}