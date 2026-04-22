package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.dto.ApiResponseDto;
import by.step.dto.OrderDto;
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
 * Веб-контроллер для работы художника с заказами.
 * Обеспечивает отображение заказов художника и действия над ними
 * (принять, отклонить, завершить, отправить на проверку).
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/artist")
@RequiredArgsConstructor
public class ArtistOrderController {

    private final BusinessServiceClient businessServiceClient;

    /**
     * Отображает список заказов для текущего художника с пагинацией.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param model модель для передачи данных
     * @return имя шаблона списка заказов художника
     */
    @GetMapping("/orders")
    public String getArtistOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {

        Long artistId = getCurrentUserId();
        if (artistId == null) {
            return "redirect:/login";
        }

        log.debug("Отображение заказов художника {}: page={}, size={}, sort={}, direction={}",
                artistId, page, size, sort, direction);

        try {
            ApiResponseDto<Page<OrderDto>> response = businessServiceClient.getOrdersByArtistPage(
                    artistId, page, size, sort, direction);

            if (response != null && response.getData() != null) {
                Page<OrderDto> orderPage = response.getData();
                model.addAttribute("orders", orderPage.getContent());
                model.addAttribute("currentPage", orderPage.getNumber());
                model.addAttribute("totalPages", orderPage.getTotalPages());
                model.addAttribute("totalItems", orderPage.getTotalElements());
                model.addAttribute("pageSize", size);
                model.addAttribute("sortField", sort);
                model.addAttribute("sortDirection", direction);

                if (orderPage.getTotalPages() > 0) {
                    List<Integer> pageNumbers = IntStream.rangeClosed(0, orderPage.getTotalPages() - 1)
                            .boxed()
                            .collect(Collectors.toList());
                    model.addAttribute("pageNumbers", pageNumbers);
                }
            } else {
                model.addAttribute("orders", List.of());
                model.addAttribute("totalPages", 0);
            }

        } catch (Exception e) {
            log.error("Ошибка при загрузке заказов художника: {}", e.getMessage());
            model.addAttribute("orders", List.of());
            model.addAttribute("error", "Не удалось загрузить заказы");
        }
        return "artist/orders";
    }

    /**
     * Принимает заказ в работу.
     * Меняет статус заказа с NEW на IN_PROGRESS.
     *
     * @param orderId идентификатор заказа
     * @return перенаправление на список заказов художника
     */
    @PostMapping("/orders/{orderId}/accept")
    public String acceptOrder(@PathVariable Long orderId) {
        log.info("Художник принимает заказ: {}", orderId);
        businessServiceClient.startOrder(orderId);
        return "redirect:/artist/orders";
    }

    /**
     * Отклоняет заказ.
     * Меняет статус заказа на CANCELLED.
     *
     * @param orderId идентификатор заказа
     * @return перенаправление на список заказов художника
     */
    @PostMapping("/orders/{orderId}/reject")
    public String rejectOrder(@PathVariable Long orderId) {
        log.info("Художник отклоняет заказ: {}", orderId);
        businessServiceClient.cancelOrder(orderId);
        return "redirect:/artist/orders";
    }

    /**
     * Отправляет готовую работу на проверку заказчику.
     * Меняет статус заказа с IN_PROGRESS на REVIEW.
     *
     * @param orderId идентификатор заказа
     * @param finalFileUrl URL файла с готовым скином
     * @return перенаправление на список заказов художника
     */
    @PostMapping("/orders/{orderId}/submit-review")
    public String submitForReview(@PathVariable Long orderId,
                                  @RequestParam String finalFileUrl) {
        log.info("Художник отправляет заказ {} на проверку", orderId);
        businessServiceClient.submitForReview(orderId, finalFileUrl);
        return "redirect:/artist/orders";
    }

    /**
     * Завершает заказ после подтверждения заказчиком.
     * Меняет статус заказа с REVIEW на COMPLETED.
     *
     * @param orderId идентификатор заказа
     * @return перенаправление на список заказов художника
     */
    @PostMapping("/orders/{orderId}/complete")
    public String completeOrder(@PathVariable Long orderId) {
        log.info("Художник завершает заказ: {}", orderId);
        businessServiceClient.completeOrder(orderId);
        return "redirect:/artist/orders";
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