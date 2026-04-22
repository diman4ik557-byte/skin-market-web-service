package by.step.advice;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Глобальный обработчик исключений для веб-контроллеров.
 * Перехватывает исключения и показывает понятные сообщения пользователю.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public String handleFeignException(FeignException e, Model model) {
        log.error("Ошибка при обращении к другому сервису: {}", e.getMessage());

        String errorMessage;
        if (e.status() == 404) {
            errorMessage = "Запрашиваемые данные не найдены";
        } else if (e.status() == 403) {
            errorMessage = "У вас нет прав для выполнения этого действия";
        } else if (e.status() == 401) {
            errorMessage = "Требуется авторизация";
        } else {
            errorMessage = "Произошла ошибка на сервере. Пожалуйста, попробуйте позже.";
        }

        model.addAttribute("error", errorMessage);
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        log.error("Ошибка валидации: {}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("Необработанная ошибка: ", e);
        model.addAttribute("error", "Произошла непредвиденная ошибка");
        return "error";
    }
}