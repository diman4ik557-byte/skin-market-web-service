package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.dto.ApiResponseDto;
import by.step.dto.ArtistProfileDto;
import by.step.dto.StudioDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Веб-контроллер для работы со студиями художников.
 * Обеспечивает просмотр списка студий, деталей студии,
 * создание студии, подачу заявок и управление участниками.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/studios")
@RequiredArgsConstructor
public class StudioController {

    private final BusinessServiceClient businessServiceClient;

    /**
     * Отображает список всех студий с пагинацией.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param model модель для передачи данных
     * @return имя шаблона списка студий
     */
    @GetMapping
    public String listStudios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            Model model) {

        log.debug("Отображение списка студий: page={}, size={}, sort={}, direction={}", page, size, sort, direction);

        try {
            ApiResponseDto<Page<StudioDto>> response = businessServiceClient.getAllStudiosPage(
                    page, size, sort, direction);

            if (response != null && response.getData() != null) {
                Page<StudioDto> studioPage = response.getData();
                model.addAttribute("studios", studioPage.getContent());
                model.addAttribute("currentPage", studioPage.getNumber());
                model.addAttribute("totalPages", studioPage.getTotalPages());
                model.addAttribute("totalItems", studioPage.getTotalElements());
                model.addAttribute("pageSize", size);
                model.addAttribute("sortField", sort);
                model.addAttribute("sortDirection", direction);

                if (studioPage.getTotalPages() > 0) {
                    List<Integer> pageNumbers = IntStream.rangeClosed(0, studioPage.getTotalPages() - 1)
                            .boxed()
                            .collect(Collectors.toList());
                    model.addAttribute("pageNumbers", pageNumbers);
                }
            } else {
                model.addAttribute("studios", List.of());
                model.addAttribute("totalPages", 0);
            }

        } catch (Exception e) {
            log.error("Ошибка при загрузке списка студий: {}", e.getMessage());
            model.addAttribute("studios", List.of());
            model.addAttribute("error", "Не удалось загрузить список студий");
        }
        return "studios/list";
    }

    /**
     * Отображает детальную информацию о студии.
     * Показывает описание студии, участников и чат.
     *
     * @param studioId идентификатор студии
     * @param model модель для передачи данных
     * @return имя шаблона деталей студии
     */
    @GetMapping("/{studioId}")
    public String studioDetails(@PathVariable Long studioId, Model model) {
        log.debug("Отображение деталей студии: {}", studioId);

        try {
            ApiResponseDto<StudioDto> studioResp = businessServiceClient.getStudioById(studioId);

            if (studioResp == null || studioResp.getData() == null) {
                model.addAttribute("studio", null);
                return "studios/details";
            }

            StudioDto studio = studioResp.getData();
            model.addAttribute("studio", studio);

            try {
                ApiResponseDto<List<ArtistProfileDto>> membersResp = businessServiceClient.getStudioMembers(studioId);
                model.addAttribute("members", membersResp != null && membersResp.getData() != null
                        ? membersResp.getData() : List.of());
            } catch (Exception e) {
                log.error("Ошибка при загрузке участников: {}", e.getMessage());
                model.addAttribute("members", List.of());
            }

            Long currentUserId = getCurrentUserId();
            boolean isManager = currentUserId != null && studio.getManagerId() != null
                    && currentUserId.equals(studio.getManagerId());

            boolean isAdmin = false;
            if (currentUserId != null) {
                try {
                    ApiResponseDto<UserDto> userResp = businessServiceClient.getUserById(currentUserId);
                    isAdmin = userResp != null && userResp.getData() != null
                            && "ADMIN".equals(userResp.getData().getRole().name());
                } catch (Exception e) {
                    log.error("Ошибка проверки роли администратора: {}", e.getMessage());
                }
            }

            model.addAttribute("isManager", isManager);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUserId", currentUserId);

        } catch (Exception e) {
            log.error("Ошибка при загрузке студии {}: {}", studioId, e.getMessage());
            model.addAttribute("studio", null);
        }
        return "studios/details";
    }

    /**
     * Отображает форму создания новой студии.
     *
     * @return имя шаблона формы создания студии
     */
    @GetMapping("/create")
    public String showCreateForm() {
        return "studios/create";
    }

    /**
     * Обрабатывает создание новой студии.
     *
     * @param name название студии
     * @param description описание студии
     * @return перенаправление на список студий
     */
    @PostMapping("/create")
    public String createStudio(@RequestParam String name, @RequestParam String description) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        log.info("Создание студии: name={}, userId={}", name, userId);
        businessServiceClient.createStudio(userId, name, description);
        return "redirect:/studios";
    }

    /**
     * Подаёт заявку на вступление в студию от имени текущего художника.
     *
     * @param studioId идентификатор студии
     * @return перенаправление на страницу деталей студии
     */
    @PostMapping("/{studioId}/request")
    public String requestToJoin(@PathVariable Long studioId) {
        Long artistId = getCurrentUserId();
        if (artistId == null) {
            return "redirect:/login";
        }
        log.info("Заявка на вступление в студию {} от художника {}", studioId, artistId);
        businessServiceClient.requestToJoinStudio(studioId, artistId);
        return "redirect:/studios/" + studioId;
    }

    /**
     * Удаляет участника из студии (только для менеджера).
     *
     * @param studioId идентификатор студии
     * @param artistId идентификатор художника
     * @return перенаправление на страницу деталей студии
     */
    @PostMapping("/{studioId}/members/{artistId}/remove")
    public String removeMember(@PathVariable Long studioId, @PathVariable Long artistId) {
        Long managerId = getCurrentUserId();
        if (managerId == null) {
            return "redirect:/login";
        }
        log.info("Удаление художника {} из студии {} менеджером {}", artistId, studioId, managerId);
        businessServiceClient.removeStudioMember(studioId, artistId, managerId);
        return "redirect:/studios/" + studioId;
    }

    /**
     * Удаляет студию (только для менеджера).
     *
     * @param studioId идентификатор студии
     * @return перенаправление на список студий
     */
    @PostMapping("/{studioId}/delete")
    public String deleteStudio(@PathVariable Long studioId) {
        Long managerId = getCurrentUserId();
        if (managerId == null) {
            return "redirect:/login";
        }
        log.info("Удаление студии {} менеджером {}", studioId, managerId);
        businessServiceClient.deleteStudio(studioId, managerId);
        return "redirect:/studios";
    }

    /**
     * Возвращает ID текущего авторизованного пользователя.
     *
     * @return ID пользователя или null если не авторизован
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }

        String username = auth.getName();
        try {
            ApiResponseDto<UserDto> response = businessServiceClient.getUserByUsername(username);
            return response != null && response.getData() != null ? response.getData().getId() : null;
        } catch (Exception e) {
            log.error("Ошибка получения ID пользователя: {}", e.getMessage());
            return null;
        }
    }
}