package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.dto.ApiResponseDto;
import by.step.dto.CreateOrderRequestDto;
import by.step.dto.OrderDto;
import by.step.dto.UserDto;
import jakarta.validation.Valid;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Веб-контроллер для работы с заказами.
 * Обеспечивает отображение списка заказов, создание новых заказов
 * и выполнение действий над существующими заказами.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final BusinessServiceClient businessServiceClient;

    /**
     * Возвращает ID текущего авторизованного пользователя.
     *
     * @return ID пользователя
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

    /**
     * Отображает список заказов текущего пользователя с пагинацией.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param model для передачи данных
     * @return имя шаблона списка заказов
     */
    @GetMapping
    public String getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return "redirect:/login";
        }

        log.debug("Отображение заказов пользователя {}: page={}, size={}, sort={}, direction={}",
                currentUserId, page, size, sort, direction);

        try {
            ApiResponseDto<Page<OrderDto>> response = businessServiceClient.getOrdersByCustomerPage(
                    currentUserId, page, size, sort, direction);

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
            log.error("Ошибка при загрузке заказов: {}", e.getMessage());
            model.addAttribute("orders", List.of());
            model.addAttribute("error", "Не удалось загрузить заказы");
        }
        return "orders/list";
    }

    /**
     * Отображает список заказов указанного заказчика с пагинацией.
     *
     * @param customerId идентификатор заказчика
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param model для передачи данных
     * @return имя шаблона списка заказов
     */
    @GetMapping("/customer/{customerId}")
    public String getOrdersByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {

        log.debug("Отображение заказов заказчика {}: page={}", customerId, page);

        try {
            ApiResponseDto<Page<OrderDto>> ordersResponse = businessServiceClient.getOrdersByCustomerPage(
                    customerId, page, size, sort, direction);
            ApiResponseDto<UserDto> customerResponse = businessServiceClient.getUserById(customerId);

            if (ordersResponse != null && ordersResponse.getData() != null) {
                Page<OrderDto> orderPage = ordersResponse.getData();
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

            model.addAttribute("customer", customerResponse != null ? customerResponse.getData() : null);

        } catch (Exception e) {
            log.error("Ошибка при загрузке заказов заказчика: {}", e.getMessage());
            model.addAttribute("orders", List.of());
            model.addAttribute("error", "Не удалось загрузить заказы");
        }
        return "orders/list";
    }


    /**
     * Отображает список заказов указанного художника с пагинацией.
     *
     * @param artistId идентификатор художника
     * @param page номер страницы
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param model для передачи данных
     * @return имя шаблона списка заказов
     */
    @GetMapping("/artist/{artistId}")
    public String getOrdersByArtist(
            @PathVariable Long artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            Model model) {

        log.debug("Отображение заказов художника {}: page={}", artistId, page);

        try {
            ApiResponseDto<Page<OrderDto>> ordersResponse = businessServiceClient.getOrdersByArtistPage(
                    artistId, page, size, sort, direction);
            ApiResponseDto<UserDto> artistResponse = businessServiceClient.getUserById(artistId);

            if (ordersResponse != null && ordersResponse.getData() != null) {
                Page<OrderDto> orderPage = ordersResponse.getData();
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

            model.addAttribute("artist", artistResponse != null ? artistResponse.getData() : null);

        } catch (Exception e) {
            log.error("Ошибка при загрузке заказов художника: {}", e.getMessage());
            model.addAttribute("orders", List.of());
            model.addAttribute("error", "Не удалось загрузить заказы");
        }
        return "orders/list";
    }

    /**
     * Отображает детальную информацию о заказе.
     *
     * @param orderId идентификатор заказа
     * @param model для передачи данных
     * @return имя шаблона деталей заказа
     */
    @GetMapping("/{orderId}")
    public String getOrderById(@PathVariable Long orderId, Model model) {
        log.debug("Отображение деталей заказа: {}", orderId);

        try {
            ApiResponseDto<OrderDto> response = businessServiceClient.getOrderById(orderId);
            OrderDto order = response != null && response.getData() != null ? response.getData() : null;
            model.addAttribute("order", order);
        } catch (Exception e) {
            log.error("Ошибка при загрузке заказа {}: {}", orderId, e.getMessage());
            model.addAttribute("error", "Не удалось загрузить заказ");
        }
        return "orders/details";
    }

    /**
     * Отображает форму создания нового заказа.
     *
     * @param artistId идентификатор предвыбранного художника (опционально)
     * @param model для передачи данных
     * @return имя шаблона формы создания заказа
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long artistId, Model model) {
        log.debug("Отображение формы создания заказа");

        model.addAttribute("order", new CreateOrderRequestDto());

        try {
            ApiResponseDto<List<UserDto>> response = businessServiceClient.getAllUsers();
            List<UserDto> artists = response != null && response.getData() != null
                    ? response.getData().stream()
                    .filter(user -> "ARTIST".equals(user.getRole().name()))
                    .collect(Collectors.toList())
                    : List.of();
            model.addAttribute("artists", artists);

            if (artistId != null) {
                ApiResponseDto<UserDto> artistResponse = businessServiceClient.getUserById(artistId);
                if (artistResponse != null && artistResponse.getData() != null) {
                    model.addAttribute("selectedArtist", artistResponse.getData());
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при загрузке списка художников: {}", e.getMessage());
            model.addAttribute("artists", List.of());
            model.addAttribute("error", "Не удалось загрузить список художников");
        }

        return "orders/create";
    }

    /**
     * Обрабатывает создание нового заказа.
     *
     * @param request DTO с данными заказа
     * @param result результат валидации
     * @param model для передачи данных
     * @return перенаправление на список заказов или обратно на форму
     */
    @PostMapping("/create")
    public String createOrder(@Valid @ModelAttribute("order") CreateOrderRequestDto request,
                              BindingResult result,
                              Model model) {

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return "redirect:/login";
        }

        request.setCustomerId(currentUserId);

        if (result.hasErrors()) {
            log.warn("Ошибки валидации при создании заказа: {}", result.getAllErrors());

            try {
                ApiResponseDto<List<UserDto>> response = businessServiceClient.getAllUsers();
                List<UserDto> artists = response != null && response.getData() != null
                        ? response.getData().stream()
                        .filter(u -> "ARTIST".equals(u.getRole().name()))
                        .collect(Collectors.toList())
                        : List.of();
                model.addAttribute("artists", artists);
            } catch (Exception e) {
                model.addAttribute("artists", List.of());
            }

            model.addAttribute("order", request);
            return "orders/create";
        }

        log.info("Создание заказа: customerId={}, artistId={}, price={}",
                request.getCustomerId(), request.getArtistId(), request.getPrice());

        businessServiceClient.createOrder(request);
        return "redirect:/orders";
    }

    /**
     * Начинает выполнение заказа.
     *
     * @param orderId идентификатор заказа
     * @return перенаправление на страницу деталей заказа
     */
    @PostMapping("/{orderId}/start")
    public String startOrder(@PathVariable Long orderId) {
        log.info("Начало выполнения заказа: {}", orderId);
        businessServiceClient.startOrder(orderId);
        return "redirect:/orders/" + orderId;
    }

    /**
     * Отправляет заказ на проверку заказчику.
     *
     * @param orderId идентификатор заказа
     * @param finalFileUrl URL файла для проверки
     * @return перенаправление на страницу деталей заказа
     */
    @PostMapping("/{orderId}/submit-review")
    public String submitForReview(@PathVariable Long orderId,
                                  @RequestParam(required = false) String finalFileUrl) {
        log.info("Отправка заказа {} на проверку", orderId);
        String fileUrl = finalFileUrl != null ? finalFileUrl : "/uploads/review/" + orderId + ".png";
        businessServiceClient.submitForReview(orderId, fileUrl);
        return "redirect:/orders/" + orderId;
    }

    /**
     * Завершает заказ.
     *
     * @param orderId идентификатор заказа
     * @return перенаправление на страницу деталей заказа
     */
    @PostMapping("/{orderId}/complete")
    public String completeOrder(@PathVariable Long orderId) {
        log.info("Завершение заказа: {}", orderId);
        businessServiceClient.completeOrder(orderId);
        return "redirect:/orders/" + orderId;
    }

    /**
     * Отменяет заказ.
     *
     * @param orderId идентификатор заказа
     * @return перенаправление на страницу деталей заказа
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId) {
        log.info("Отмена заказа: {}", orderId);
        businessServiceClient.cancelOrder(orderId);
        return "redirect:/orders/" + orderId;
    }
}