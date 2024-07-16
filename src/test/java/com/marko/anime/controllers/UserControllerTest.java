package com.marko.anime.controllers;

import com.marko.anime.configuration.TestSecurityConfig;
import com.marko.anime.dtos.UserInfoDTO;
import com.marko.anime.repositories.TokenRepository;
import com.marko.anime.services.JwtService;
import com.marko.anime.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserInfo_shouldReturnStatusOkAndUserInfo() throws Exception {
        List<UserInfoDTO> userList = List.of(
                new UserInfoDTO("1", "John Wick", "ROLE_USER"),
                new UserInfoDTO("2", "Anakin Skywalker", "ROLE_USER")
        );
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("1"))
                .andExpect(jsonPath("$[0].email").value("John Wick"))
                .andExpect(jsonPath("$[0].role").value("ROLE_USER"))
                .andExpect(jsonPath("$[1].userId").value("2"))
                .andExpect(jsonPath("$[1].email").value("Anakin Skywalker"))
                .andExpect(jsonPath("$[1].role").value("ROLE_USER"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_shouldBanUserAndReturnNoContent() throws Exception {
        String userId = "23";
        doNothing().when(userService).banUser(userId);

        mockMvc.perform(put("/api/v1/users/{userId}/ban", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).banUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void banUser_shouldNotBanAdminUser() throws Exception {
        String adminId = "adminId";
        doNothing().when(userService).banUser(adminId);

        mockMvc.perform(put("/api/v1/users/{userId}/ban", adminId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).banUser(adminId);
    }

}
