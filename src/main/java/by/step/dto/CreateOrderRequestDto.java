package by.step.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для создания нового заказа.
 * Содержит данные, необходимые для создания заказа: кто заказывает, у кого, что и за сколько.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDto {

    /**
     * ID заказчика (не может быть негативным).
     */
    @Positive(message = "ID заказчика обязателен")
    @Positive(message = "ID заказчика должен быть положительным числом")
    private Long customerId;

    /**
     * ID художника (не может быть null).
     */
    @NotNull(message = "ID художника обязателен")
    @Positive(message = "ID художника должен быть положительным числом")
    private Long artistId;

    /**
     * Описание заказа (не может быть пустым).
     */
    @NotBlank(message = "Описание заказа обязательно")
    @Size(min = 5, max = 5000, message = "Описание должно быть от 10 до 5000 символов")
    private String description;

    /**
     * Цена заказа (должна быть положительной).
     */
    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "1.00", message = "Цена должна быть не менее 1")
    @DecimalMax(value = "100000.00", message = "Цена не может превышать 100000")
    private BigDecimal price;
}