package by.step.controller;

import by.step.client.AuthServiceClient;
import by.step.client.BusinessServiceClient;
import by.step.dto.AuthResponseDto;
import by.step.dto.RegistrationRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для обработки запросов регистрации новых пользователей.
 * Обеспечивает отображение формы регистрации и обработку данных формы.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final AuthServiceClient authServiceClient;
    private final BusinessServiceClient businessServiceClient;

    /**
     * Отображает форму регистрации нового пользователя.
     *
     * @param model для передачи данных в представление
     * @return имя шаблона страницы регистрации
     */
    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationRequestDto());
        return "register";
    }

    /**
     * Обрабатывает отправку формы регистрации.
     * Валидирует входные данные и отправляет запрос на создание пользователя в auth-сервис.
     *
     * @param registrationDto DTO с данными регистрации
     * @param result результат валидации
     * @param model для передачи сообщений об ошибках
     * @return перенаправление на страницу входа или обратно на форму с ошибкой
     */
    @PostMapping
    public String registerUser(@Valid @ModelAttribute("user") RegistrationRequestDto registrationDto,
                               BindingResult result,
                               Model model) {

        model.addAttribute("error", null);
        model.addAttribute("success", null);

        if (result.hasErrors()) {
            log.warn("Ошибки валидации при регистрации: {}", result.getAllErrors());
            return "register";
        }

        try {
            AuthResponseDto authResponse = authServiceClient.register(registrationDto);
            log.info("Пользователь зарегистрирован в auth-service: {}", authResponse.getUsername());

            if (registrationDto.getRole() == null || registrationDto.getRole().isEmpty()) {
                registrationDto.setRole("USER");
            }

            businessServiceClient.registerUser(registrationDto);
            log.info("Пользователь создан в data-service: {}", registrationDto.getUsername());

            model.addAttribute("success", "Регистрация успешна! Теперь вы можете войти.");
            model.addAttribute("user", new RegistrationRequestDto());
            return "register";

        } catch (Exception e) {
            log.error("Ошибка регистрации: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}