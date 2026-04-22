package by.step.client;

import by.step.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "business-service", url = "${business-service.url:http://localhost:8082}")
public interface BusinessServiceClient {

    // ==================== USER ENDPOINTS ====================
    @GetMapping("/api/business/users/{id}")
    ApiResponseDto<UserDto> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/business/users/username/{username}")
    ApiResponseDto<UserDto> getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/api/business/users")
    ApiResponseDto<List<UserDto>> getAllUsers();

    @GetMapping("/api/business/users/role/{role}")
    ApiResponseDto<List<UserDto>> getUsersByRole(@PathVariable("role") String role);

    @PutMapping("/api/business/users/{userId}/role")
    ApiResponseDto<UserDto> updateUserRole(@PathVariable("userId") Long userId, @RequestParam String role);

    @PostMapping("/api/business/users/register")
    ApiResponseDto<UserDto> registerUser(@RequestBody RegistrationRequestDto request);

    // ==================== ORDER ENDPOINTS ====================
    @PostMapping("/api/business/orders")
    ApiResponseDto<OrderDto> createOrder(@RequestBody CreateOrderRequestDto request);

    @GetMapping("/api/business/orders/{orderId}")
    ApiResponseDto<OrderDto> getOrderById(@PathVariable("orderId") Long orderId);

    @GetMapping("/api/business/orders/customer/{customerId}")
    ApiResponseDto<List<OrderDto>> getOrdersByCustomer(@PathVariable("customerId") Long customerId);

    @GetMapping("/api/business/orders/artist/{artistId}")
    ApiResponseDto<List<OrderDto>> getOrdersByArtist(@PathVariable("artistId") Long artistId);

    // Методы с пагинацией
    @GetMapping("/api/business/orders/customer/{customerId}/page")
    ApiResponseDto<Page<OrderDto>> getOrdersByCustomerPage(
            @PathVariable("customerId") Long customerId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,
            @RequestParam("direction") String direction
    );

    @GetMapping("/api/business/orders/artist/{artistId}/page")
    ApiResponseDto<Page<OrderDto>> getOrdersByArtistPage(
            @PathVariable("artistId") Long artistId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,
            @RequestParam("direction") String direction
    );

    @PostMapping("/api/business/orders/{orderId}/start")
    void startOrder(@PathVariable("orderId") Long orderId);

    @PostMapping("/api/business/orders/{orderId}/submit-review")
    void submitForReview(@PathVariable("orderId") Long orderId, @RequestParam String finalFileUrl);

    @PostMapping("/api/business/orders/{orderId}/complete")
    void completeOrder(@PathVariable("orderId") Long orderId);

    @PostMapping("/api/business/orders/{orderId}/cancel")
    void cancelOrder(@PathVariable("orderId") Long orderId);

    // ==================== USER PAGE ENDPOINTS ====================
    @GetMapping("/api/business/users/role/{role}/page")
    ApiResponseDto<Page<UserDto>> getUsersByRolePage(
            @PathVariable("role") String role,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,
            @RequestParam("direction") String direction
    );

    @GetMapping("/api/business/users/page")
    ApiResponseDto<Page<UserDto>> getAllUsersPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,
            @RequestParam("direction") String direction
    );

    // ==================== STUDIO ENDPOINTS ====================
    @PostMapping("/api/business/studios")
    ApiResponseDto<StudioDto> createStudio(@RequestParam Long userId,
                                           @RequestParam String name,
                                           @RequestParam String description);

    @GetMapping("/api/business/studios/{studioId}")
    ApiResponseDto<StudioDto> getStudioById(@PathVariable("studioId") Long studioId);

    @GetMapping("/api/business/studios")
    ApiResponseDto<List<StudioDto>> getAllStudios();

    @GetMapping("/api/business/studios/user/{userId}")
    ApiResponseDto<StudioDto> getStudioByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/api/business/studios/{studioId}/members")
    ApiResponseDto<List<ArtistProfileDto>> getStudioMembers(@PathVariable("studioId") Long studioId);

    @PostMapping("/api/business/studios/{studioId}/members/{artistId}/request")
    ApiResponseDto<Void> requestToJoinStudio(@PathVariable("studioId") Long studioId,
                                             @PathVariable("artistId") Long artistId);

    @PostMapping("/api/business/studios/{studioId}/members/{artistId}/approve")
    ApiResponseDto<Void> approveStudioMember(@PathVariable("studioId") Long studioId,
                                             @PathVariable("artistId") Long artistId,
                                             @RequestParam Long managerId);

    @DeleteMapping("/api/business/studios/{studioId}/members/{artistId}")
    ApiResponseDto<Void> removeStudioMember(@PathVariable("studioId") Long studioId,
                                            @PathVariable("artistId") Long artistId,
                                            @RequestParam Long managerId);

    @PutMapping("/api/business/studios/{studioId}")
    ApiResponseDto<StudioDto> updateStudio(@PathVariable("studioId") Long studioId,
                                           @RequestParam String description,
                                           @RequestParam Long managerId);

    @DeleteMapping("/api/business/studios/{studioId}")
    ApiResponseDto<Void> deleteStudio(@PathVariable("studioId") Long studioId,
                                      @RequestParam Long managerId);

    @GetMapping("/api/business/studios/{studioId}/is-manager")
    boolean isManager(@PathVariable("studioId") Long studioId,
                      @RequestParam Long userId);

    @GetMapping("/api/business/studios/page")
    ApiResponseDto<Page<StudioDto>> getAllStudiosPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,
            @RequestParam("direction") String direction
    );

    // ==================== MESSAGE ENDPOINTS ====================
    @GetMapping("/api/business/messages/order/{orderId}")
    ApiResponseDto<Page<MessageDto>> getMessagesByOrder(
            @PathVariable("orderId") Long orderId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,
            @RequestParam("direction") String direction
    );

    @PostMapping("/api/business/messages/order/{orderId}/send")
    ApiResponseDto<MessageDto> sendMessage(@PathVariable("orderId") Long orderId,
                                           @RequestParam Long senderId,
                                           @RequestParam String content);

    @PostMapping("/api/business/messages/order/{orderId}/preview")
    ApiResponseDto<MessageDto> sendPreview(@PathVariable("orderId") Long orderId,
                                           @RequestParam Long senderId,
                                           @RequestParam String content,
                                           @RequestParam String attachmentUrl);

    @PostMapping("/api/business/messages/order/{orderId}/attachment")
    ApiResponseDto<MessageDto> sendAttachment(@PathVariable("orderId") Long orderId,
                                              @RequestParam Long senderId,
                                              @RequestParam String attachmentUrl);

    @GetMapping("/api/business/messages/studio/{studioId}")
    ApiResponseDto<List<MessageDto>> getStudioMessages(@PathVariable("studioId") Long studioId);

    @PostMapping("/api/business/messages/studio/{studioId}/send")
    ApiResponseDto<MessageDto> sendToStudio(@PathVariable("studioId") Long studioId,
                                            @RequestParam Long senderId,
                                            @RequestParam String content,
                                            @RequestParam(required = false) String attachmentUrl);
}