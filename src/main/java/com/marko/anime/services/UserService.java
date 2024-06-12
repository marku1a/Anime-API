package com.marko.anime.services;

import com.marko.anime.dtos.UserInfoDTO;
import com.marko.anime.models.User;
import com.marko.anime.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<UserInfoDTO> getAllUsers() {
        List<User> full = userRepository.findAll();
        return full.stream()
                .filter(User::isAccountNonLocked)
                .map(user-> new UserInfoDTO(user.getUserId(), user.getEmail(), user.getRole()))
                .toList();
    }
    public void banUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getRole().equals("ROLE_ADMIN")) {
            return;
        }
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }
}
