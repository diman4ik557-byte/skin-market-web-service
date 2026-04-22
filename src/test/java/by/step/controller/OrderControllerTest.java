package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.config.JwtTokenProvider;
import by.step.dto.ApiResponseDto;
import by.step.dto.OrderDto;
import by.step.dto.UserDto;
import by.step.enums.OrderStatus;
import by.step.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessServiceClient businessServiceClient;

    private OrderDto testOrderDto;
    private UserDto testArtistDto;
    private ApiResponseDto<OrderDto> orderResponse;
    private ApiResponseDto<Page<OrderDto>> ordersPageResponse;
    private ApiResponseDto<List<UserDto>> usersResponse;
    private Page<OrderDto> orderPage;
    private ApiResponseDto<UserDto> userResponse;

    @BeforeEach
    void setUp() {
        testOrderDto = OrderDto.builder()
                .id(1L)
                .customerId(1L)
                .customerName("testuser")
                .artistId(2L)
                .artistName("artist")
                .status(OrderStatus.NEW)
                .description("Test order description")
                .price(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();

        testArtistDto = UserDto.builder()
                .id(2L)
                .username("artist")
                .role(UserRole.ARTIST)
                .build();

        orderResponse = ApiResponseDto.success(testOrderDto);

        orderPage = new PageImpl<>(List.of(testOrderDto), PageRequest.of(0, 10), 1);
        ordersPageResponse = ApiResponseDto.success(orderPage);

        usersResponse = ApiResponseDto.success(List.of(testArtistDto));
    }

  /*  @Test
    @WithMockUser(username = "testuser")
    void getAllOrders_ShouldReturnOrdersListView() throws Exception {
        when(businessServiceClient.getUserByUsername("testuser")).thenReturn(userResponse);
        when(businessServiceClient.getOrdersByCustomerPage(eq(1L), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(ordersPageResponse);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeExists("orders"));
    }*/

    @Test
    @WithMockUser
    void getOrdersByCustomer_ShouldReturnOrdersListView() throws Exception {
        when(businessServiceClient.getOrdersByCustomerPage(anyLong(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(ordersPageResponse);
        when(businessServiceClient.getUserById(anyLong())).thenReturn(
                ApiResponseDto.success(UserDto.builder().id(1L).username("customer").build()));

        mockMvc.perform(get("/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeExists("orders", "customer"));
    }

    @Test
    @WithMockUser
    void getOrdersByArtist_ShouldReturnOrdersListView() throws Exception {
        when(businessServiceClient.getOrdersByArtistPage(anyLong(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(ordersPageResponse);
        when(businessServiceClient.getUserById(anyLong())).thenReturn(
                ApiResponseDto.success(UserDto.builder().id(2L).username("artist").build()));

        mockMvc.perform(get("/orders/artist/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeExists("orders", "artist"));
    }

    @Test
    @WithMockUser
    void getOrderById_ShouldReturnOrderDetailsView() throws Exception {
        when(businessServiceClient.getOrderById(anyLong())).thenReturn(orderResponse);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/details"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @WithMockUser
    void showCreateForm_ShouldReturnCreateOrderView() throws Exception {
        when(businessServiceClient.getAllUsers()).thenReturn(usersResponse);

        mockMvc.perform(get("/orders/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/create"))
                .andExpect(model().attributeExists("order", "artists"));
    }

    /*@Test
    @WithMockUser
    void createOrder_ShouldRedirectToOrders() throws Exception {
        when(businessServiceClient.createOrder(any())).thenReturn(orderResponse);

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .param("artistId", "2")
                        .param("description", "New order")
                        .param("price", "150"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));
    }*/

    @Test
    @WithMockUser
    void startOrder_ShouldRedirectToOrderDetails() throws Exception {
        doNothing().when(businessServiceClient).startOrder(anyLong());

        mockMvc.perform(post("/orders/1/start")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1"));
    }

    @Test
    @WithMockUser
    void completeOrder_ShouldRedirectToOrderDetails() throws Exception {
        doNothing().when(businessServiceClient).completeOrder(anyLong());

        mockMvc.perform(post("/orders/1/complete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1"));
    }

    @Test
    @WithMockUser
    void cancelOrder_ShouldRedirectToOrderDetails() throws Exception {
        doNothing().when(businessServiceClient).cancelOrder(anyLong());

        mockMvc.perform(post("/orders/1/cancel")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1"));
    }
}