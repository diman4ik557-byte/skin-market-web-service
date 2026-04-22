package by.step.controller;

import by.step.client.BusinessServiceClient;
import by.step.config.JwtTokenProvider;
import by.step.dto.ApiResponseDto;
import by.step.dto.UserDto;
import by.step.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessServiceClient businessServiceClient;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UserDto testUserDto;
    private ApiResponseDto<UserDto> userResponse;
    private ApiResponseDto<List<UserDto>> usersResponse;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .balance(BigDecimal.valueOf(1000))
                .registeredAt(LocalDateTime.now())
                .build();

        userResponse = ApiResponseDto.success(testUserDto);
        usersResponse = ApiResponseDto.success(List.of(testUserDto));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void profile_ShouldReturnProfileView() throws Exception {
        when(businessServiceClient.getUserByUsername(anyString())).thenReturn(userResponse);

        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllUsers_ShouldReturnUsersListView() throws Exception {
        when(businessServiceClient.getAllUsers()).thenReturn(usersResponse);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/list"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser
    void getUserById_ShouldReturnUserDetailsView() throws Exception {
        when(businessServiceClient.getUserById(anyLong())).thenReturn(userResponse);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/details"))
                .andExpect(model().attributeExists("user"));
    }
}