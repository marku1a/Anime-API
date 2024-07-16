package com.marko.anime.services;

import com.marko.anime.dtos.UserInfoDTO;
import com.marko.anime.models.User;
import com.marko.anime.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private User adminUser;

    @BeforeEach
    void init() {
        user1 = new User();
        user1.setUserId("user1");
        user1.setEmail("user1@mail.com");
        user1.setRole("ROLE_USER");
        user1.setAccountNonLocked(true);

        user2 = new User();
        user2.setUserId("user2");
        user2.setEmail("user2@mail.com");
        user2.setRole("ROLE_USER");
        user2.setAccountNonLocked(false);

        adminUser = new User();
        adminUser.setUserId("admin");
        adminUser.setEmail("admin@mail.com");
        adminUser.setRole("ROLE_ADMIN");
        adminUser.setAccountNonLocked(true);
    }

    @Test
    void getAllUsers_ShouldReturnNonLockedUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2, adminUser));

        List<UserInfoDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(2);

        assertThat(result).extracting("userId").containsExactlyInAnyOrder("user1", "admin");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void banUser_ShouldBanNonAdminUser() {
        when(userRepository.findByUserId("user1")).thenReturn(Optional.of(user1));

        userService.banUser("user1");

        assertThat(user1.isAccountNonLocked()).isFalse();

        verify(userRepository, times(1)).findByUserId("user1");
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void banUser_ShouldNotBanAdminUser() {
        when(userRepository.findByUserId("admin")).thenReturn(Optional.of(adminUser));

        userService.banUser("admin");

        assertThat(adminUser.isAccountNonLocked()).isTrue();

        verify(userRepository, times(1)).findByUserId("admin");
        verify(userRepository, never()).save(adminUser);
    }

    @Test
    void banUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUserId("user1")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.banUser("user1"));

        verify(userRepository, times(1)).findByUserId("user1");
        verify(userRepository, never()).save(any(User.class));
    }
}