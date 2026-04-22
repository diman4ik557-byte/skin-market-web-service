package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.dto.ApiResponseDto;
import by.step.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Веб-контроллер для работы с пользователями (административная часть).
 * Обеспечивает просмотр списка пользователей и деталей профиля.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final BusinessServiceClient businessServiceClient;

    /**
     * Отображает список всех пользователей с пагинацией.
     * Доступен только для администратора.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param model модель для передачи данных
     * @return имя шаблона списка пользователей
     */
    @GetMapping
    public String getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            Model model) {

        log.debug("Отображение списка пользователей: page={}, size={}, sort={}, direction={}",
                page, size, sort, direction);

        try {
            ApiResponseDto<Page<UserDto>> response = businessServiceClient.getAllUsersPage(
                    page, size, sort, direction);

            if (response != null && response.getData() != null) {
                Page<UserDto> userPage = response.getData();
                model.addAttribute("users", userPage.getContent());
                model.addAttribute("currentPage", userPage.getNumber());
                model.addAttribute("totalPages", userPage.getTotalPages());
                model.addAttribute("totalItems", userPage.getTotalElements());
                model.addAttribute("pageSize", size);
                model.addAttribute("sortField", sort);
                model.addAttribute("sortDirection", direction);

                if (userPage.getTotalPages() > 0) {
                    List<Integer> pageNumbers = IntStream.rangeClosed(0, userPage.getTotalPages() - 1)
                            .boxed()
                            .collect(Collectors.toList());
                    model.addAttribute("pageNumbers", pageNumbers);
                }
            } else {
                model.addAttribute("users", List.of());
                model.addAttribute("totalPages", 0);
            }

        } catch (Exception e) {
            log.error("Ошибка при загрузке списка пользователей: {}", e.getMessage());
            model.addAttribute("users", List.of());
            model.addAttribute("error", "Не удалось загрузить список пользователей");
        }
        return "users/list";
    }

    /**
     * Отображает детальную информацию о пользователе.
     *
     * @param id идентификатор пользователя
     * @param model модель для передачи данных
     * @return имя шаблона деталей пользователя
     */
    @GetMapping("/{id}")
    public String getUserById(@PathVariable Long id, Model model) {
        log.debug("Отображение профиля пользователя с id={}", id);

        try {
            ApiResponseDto<UserDto> response = businessServiceClient.getUserById(id);
            UserDto user = response != null && response.getData() != null ? response.getData() : null;
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("Ошибка при загрузке пользователя {}: {}", id, e.getMessage());
            model.addAttribute("error", "Не удалось загрузить пользователя");
        }
        return "users/details";
    }

    /**
     * API: Возвращает информацию о текущем авторизованном пользователе.
     * Используется в JavaScript для определения отправителя сообщений.
     *
     * @return данные текущего пользователя
     */
    @GetMapping("/profile/api")
    @ResponseBody
    public UserDto getCurrentUserApi() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "user";
        log.debug("API запрос: получение текущего пользователя {}", username);

        try {
            ApiResponseDto<UserDto> response = businessServiceClient.getUserByUsername(username);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Ошибка получения текущего пользователя: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Отображает профиль текущего авторизованного пользователя.
     * Используется в JavaScript для определения отправителя сообщений.
     *
     * @return профиль текущего пользователя
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        log.debug("Отображение профиля текущего пользователя");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;

            if (username == null) {
                log.warn("Пользователь не аутентифицирован");
                return "redirect:/login";
            }

            ApiResponseDto<UserDto> response = businessServiceClient.getUserByUsername(username);
            UserDto user = response != null && response.getData() != null ? response.getData() : null;

            if (user == null) {
                log.warn("Пользователь {} не найден", username);
                return "error";
            }

            model.addAttribute("user", user);
            return "users/profile";

        } catch (Exception e) {
            log.error("Ошибка при загрузке профиля: {}", e.getMessage());
            model.addAttribute("error", "Не удалось загрузить профиль");
            return "error";
        }
    }
}