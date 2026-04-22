package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.dto.ApiResponseDto;
import by.step.dto.MessageDto;
import by.step.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Веб-контроллер для работы с чатами.
 * Обеспечивает отображение страниц чата для заказов и студий,
 * а также API для отправки и получения сообщений.
 *
 * @author Skin Market Team
 * @version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class MessageController {

    private final BusinessServiceClient businessServiceClient;

    /**
     * Отображает страницу чата для конкретного заказа.
     *
     * @param orderId ID заказа
     * @param model для передачи ID заказа
     * @return имя шаблона чата
     */
    @GetMapping("/order/{orderId}")
    public String chatPage(@PathVariable Long orderId, Model model) {
        log.debug("Отображение чата для заказа: {}", orderId);
        model.addAttribute("orderId", orderId);
        return "chat/index";
    }

    /**
     * Отображает страницу чата для конкретной студии.
     *
     * @param studioId ID студии
     * @param model для передачи ID студии
     * @return имя шаблона чата студии
     */
    @GetMapping("/studio/{studioId}")
    public String studioChatPage(@PathVariable Long studioId, Model model) {
        log.debug("Отображение чата для студии: {}", studioId);
        model.addAttribute("studioId", studioId);
        return "chat/studio";
    }


    /**
     * Возвращает список сообщений по заказу (API).
     *
     * @param orderId ID заказа
     * @return список сообщений
     */
    @GetMapping("/api/messages/order/{orderId}")
    @ResponseBody
    public List<MessageDto> getMessages(@PathVariable Long orderId) {
        log.debug("API запрос: получение сообщений для заказа {}", orderId);
        try {
            ApiResponseDto<Page<MessageDto>> response = businessServiceClient.getMessagesByOrder(
                    orderId, 0, 100, "sentAt", "ASC");
            if (response != null && response.getData() != null) {
                return response.getData().getContent();
            }
        } catch (Exception e) {
            log.error("Ошибка получения сообщений для заказа {}: {}", orderId, e.getMessage());
        }
        return List.of();
    }

    /**
     * Отправляет сообщение в чат заказа (API).
     *
     * @param orderId ID заказа
     * @param content текст сообщения
     * @return отправленное сообщение
     */
    @PostMapping("/api/messages/send")
    @ResponseBody
    public MessageDto sendMessage(@RequestParam Long orderId, @RequestParam String content) {
        Long senderId = getCurrentUserId();
        log.info("API запрос: отправка сообщения в заказ {} от пользователя {}", orderId, senderId);
        try {
            ApiResponseDto<MessageDto> response = businessServiceClient.sendMessage(orderId, senderId, content);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage());
            return null;
        }
    }
    /**
     * Отправляет предпросмотр в чат заказа (API).
     *
     * @param orderId ID заказа
     * @param content описание предпросмотра
     * @param attachmentUrl URL изображения
     * @return отправленное сообщение
     */
    @PostMapping("/api/messages/preview")
    @ResponseBody
    public MessageDto sendPreview(@RequestParam Long orderId,
                                  @RequestParam String content,
                                  @RequestParam String attachmentUrl) {
        Long senderId = getCurrentUserId();
        log.info("API запрос: отправка предпросмотра в заказ {} от пользователя {}", orderId, senderId);
        try {
            ApiResponseDto<MessageDto> response = businessServiceClient.sendPreview(orderId, senderId, content, attachmentUrl);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Ошибка отправки предпросмотра: {}", e.getMessage());
            return null;
        }
    }
    /**
     * Отправляет файл в чат заказа (API).
     *
     * @param orderId ID заказа
     * @param attachmentUrl URL файла
     * @return отправленное сообщение
     */
    @PostMapping("/api/messages/attachment")
    @ResponseBody
    public MessageDto sendAttachment(@RequestParam Long orderId, @RequestParam String attachmentUrl) {
        Long senderId = getCurrentUserId();
        log.info("API запрос: отправка файла в заказ {} от пользователя {}", orderId, senderId);
        try {
            ApiResponseDto<MessageDto> response = businessServiceClient.sendAttachment(orderId, senderId, attachmentUrl);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Ошибка отправки файла: {}", e.getMessage());
            return null;
        }
    }


    /**
     * Возвращает список сообщений по студии (API).
     *
     * @param studioId ID студии
     * @return список сообщений
     */
    @GetMapping("/api/studio/{studioId}/messages")
    @ResponseBody
    public List<MessageDto> getStudioMessages(@PathVariable Long studioId) {
        log.debug("API запрос: получение сообщений для студии {}", studioId);
        try {
            ApiResponseDto<List<MessageDto>> response = businessServiceClient.getStudioMessages(studioId);
            return response != null && response.getData() != null ? response.getData() : List.of();
        } catch (Exception e) {
            log.error("Ошибка получения сообщений студии: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Отправляет сообщение в чат студии (API).
     *
     * @param studioId ID студии
     * @param content текст сообщения
     * @return отправленное сообщение
     */
    @PostMapping("/api/studio/{studioId}/send")
    @ResponseBody
    public MessageDto sendToStudio(@PathVariable Long studioId, @RequestParam String content) {
        Long senderId = getCurrentUserId();
        log.info("API запрос: отправка сообщения в студию {} от пользователя {}", studioId, senderId);
        try {
            ApiResponseDto<MessageDto> response = businessServiceClient.sendToStudio(studioId, senderId, content, null);
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения в студию: {}", e.getMessage());
            return null;
        }
    }


    /**
     * Возвращает ID текущего авторизованного пользователя.
     *
     * @return ID пользователя
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            log.warn("Попытка получить ID пользователя без авторизации");
            return 1L;
        }

        String username = auth.getName();
        try {
            ApiResponseDto<UserDto> response = businessServiceClient.getUserByUsername(username);
            return response != null && response.getData() != null ? response.getData().getId() : 1L;
        } catch (Exception e) {
            log.error("Ошибка получения ID пользователя: {}", e.getMessage());
            return 1L;
        }
    }
}