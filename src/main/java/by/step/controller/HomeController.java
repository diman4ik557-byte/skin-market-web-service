package by.step.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для обработки запросов главной страницы и страницы входа.
 * Отвечает за отображение публичных страниц приложения.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Controller
public class HomeController {

    /**
     * Отображает главную страницу приложения.
     * Доступна только авторизованным пользователям.
     *
     * @return имя шаблона главной страницы
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Отображает страницу входа в систему.
     * Доступна для всех пользователей (включая неавторизованных).
     *
     * @return имя шаблона страницы входа
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}