package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.dto.ApiResponseDto;
import by.step.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для управления профилем пользователя.
 * Обеспечивает просмотр профиля и смену роли пользователя.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {

    private final BusinessServiceClient businessServiceClient;



    /**
     * Обрабатывает запрос на повышение роли пользователя до художника.
     * Обновляет роль пользователя в системе.
     *
     * @param model для передачи сообщения об успехе или ошибке
     * @return перенаправление на страницу профиля
     */
    @PostMapping("/profile/become-artist")
    public String becomeArtist(Model model) {
        String username = getCurrentUsername();
        log.info("Запрос на повышение до художника от пользователя: {}", username);

        try {
            ApiResponseDto<UserDto> response = businessServiceClient.getUserByUsername(username);
            if (response != null && response.getData() != null) {
                UserDto user = response.getData();

                ApiResponseDto<UserDto> updateResponse = businessServiceClient.updateUserRole(user.getId(), "ARTIST");

                if (updateResponse != null && updateResponse.getData() != null) {
                    log.info("Пользователь {} успешно повышен до художника", username);
                    model.addAttribute("message", "Поздравляем! Теперь вы художник!");
                } else {
                    log.error("Не удалось обновить роль для пользователя: {}", username);
                    model.addAttribute("error", "Не удалось обновить роль");
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при повышении до художника: {}", e.getMessage());
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/users/profile";
    }

    /**
     * Возвращает имя текущего авторизованного пользователя.
     *
     * @return имя пользователя или "user" по умолчанию
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "user";
    }
}