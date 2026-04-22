package by.step.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Универсальный DTO для стандартизации ответов API.
 * Используется во всех REST контроллерах для единообразного формата ответа.
 *
 * @author Skin Market Team
 * @version 1.0
 * @param <T> тип данных, возвращаемых в ответе
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {

    /**
     * Флаг успешности выполнения запроса.
     */
    private boolean success;

    /**
     * Сообщение о результате выполнения (успех или ошибка).
     */
    private String message;

    /**
     * Данные, возвращаемые в ответе (если запрос успешен).
     */
    private T data;

    /**
     * Сообщение об ошибке (если запрос не успешен).
     */
    private String error;

    /**
     * Создает успешный ответ с данными.
     *
     * @param data данные для возврата
     * @param <T> тип данных
     * @return ApiResponseDto с флагом success = true
     */
    public static <T> ApiResponseDto<T> success(T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    /**
     * Создает успешный ответ с сообщением и данными.
     *
     * @param message сообщение об успехе
     * @param data данные для возврата
     * @param <T> тип данных
     * @return ApiResponseDto с флагом success = true
     */
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Создает ответ об ошибке.
     *
     * @param error сообщение об ошибке
     * @param <T> тип данных
     * @return ApiResponseDto с флагом success = false
     */
    public static <T> ApiResponseDto<T> error(String error) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    /**
     * Создает ответ об ошибке с сообщением и деталями.
     *
     * @param message общее сообщение
     * @param error детали ошибки
     * @param <T> тип данных
     * @return ApiResponseDto с флагом success = false
     */
    public static <T> ApiResponseDto<T> error(String message, String error) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
}