package com.project.controller;

import com.project.entity.User;
import com.project.repository.UserRepository;
import com.project.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserControllerTests {

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Test
    void setPasswordUpdatesSocialUserPassword() throws Exception {
        User user = new User();
        user.setEmail("social@example.com");
        user.setProvider("google");
        user.setRole("USER");
        userRepository.save(user);

        UserController.SetPasswordRequest request = new UserController.SetPasswordRequest();
        request.setEmail("social@example.com");
        request.setPassword("secret123");

        ResponseEntity<java.util.Map<String, String>> response = userController.setPassword(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("message", "Password set successfully");

        User saved = userRepository.findFirstByEmailOrderByIdAsc("social@example.com");
        assertThat(saved.getPassword()).isEqualTo(PasswordUtil.encrypt("secret123"));
    }
}
