package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.dto.ApiResponseDto;
import by.step.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Контроллер для доски объявлений художников.
 * Отображает список художников с пагинацией и профили художников.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/marketplace")
@RequiredArgsConstructor
public class ArtistMarketplaceController {

    private final BusinessServiceClient businessServiceClient;

    /**
     * Отображает доску художников с пагинацией.
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @param sort поле для сортировки
     * @param model модель для передачи данных
     * @return имя шаблона доски художников
     */
    @GetMapping
    public String marketplace(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "ASC") String direction,
            Model model) {

        log.debug("Отображение доски художников: page={}, size={}, sort={}, direction={}", page, size, sort, direction);

        try {
            ApiResponseDto<Page<UserDto>> response = businessServiceClient.getUsersByRolePage(
                    "ARTIST", page, size, sort, direction);

            if (response != null && response.getData() != null) {
                Page<UserDto> artistPage = response.getData();
                model.addAttribute("artists", artistPage.getContent());
                model.addAttribute("currentPage", artistPage.getNumber());
                model.addAttribute("totalPages", artistPage.getTotalPages());
                model.addAttribute("totalItems", artistPage.getTotalElements());
                model.addAttribute("pageSize", size);
                model.addAttribute("sortField", sort);
                model.addAttribute("sortDirection", direction);

                if (artistPage.getTotalPages() > 0) {
                    List<Integer> pageNumbers = IntStream.rangeClosed(0, artistPage.getTotalPages() - 1)
                            .boxed()
                            .collect(Collectors.toList());
                    model.addAttribute("pageNumbers", pageNumbers);
                }
            } else {
                model.addAttribute("artists", List.of());
                model.addAttribute("totalPages", 0);
            }

        } catch (Exception e) {
            log.error("Ошибка при загрузке списка художников: {}", e.getMessage());
            model.addAttribute("artists", List.of());
            model.addAttribute("error", "Не удалось загрузить список художников");
        }
        return "marketplace/index";
    }

    /**
     * Отображает профиль конкретного художника.
     *
     * @param artistId ID художника
     * @param model модель для передачи данных
     * @return имя шаблона профиля художника
     */
    @GetMapping("/artist/{artistId}")
    public String artistProfile(@PathVariable Long artistId, Model model) {
        log.debug("Отображение профиля художника: {}", artistId);

        try {
            ApiResponseDto<UserDto> response = businessServiceClient.getUserById(artistId);
            UserDto artist = response != null ? response.getData() : null;

            if (artist == null) {
                model.addAttribute("error", "Художник не найден");
                return "marketplace/artist-profile";
            }

            model.addAttribute("artist", artist);

            try {
                ApiResponseDto<List<by.step.dto.OrderDto>> ordersResponse =
                        businessServiceClient.getOrdersByArtist(artistId);
                model.addAttribute("orders", ordersResponse != null && ordersResponse.getData() != null
                        ? ordersResponse.getData() : List.of());
            } catch (Exception e) {
                log.error("Ошибка при загрузке заказов художника: {}", e.getMessage());
                model.addAttribute("orders", List.of());
            }

        } catch (Exception e) {
            log.error("Ошибка при загрузке профиля художника {}: {}", artistId, e.getMessage());
            model.addAttribute("error", "Не удалось загрузить профиль художника");
        }
        return "marketplace/artist-profile";
    }
}