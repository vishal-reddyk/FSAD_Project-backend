package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.project.entity.User;
import com.project.repository.UserRepository;
import com.project.util.PasswordUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final List<String> ALLOWED_ADMIN_EMAILS = Arrays.asList(
            "karrivishalreddy6@gmail.com",
            "ravigunisetti99@gmail.com",
            "lonelymanwastaken@gmail.com");

    @Autowired
    private UserRepository userRepository;

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean isAllowedAdminEmail(String email) {
        return ALLOWED_ADMIN_EMAILS.contains(normalizeEmail(email));
    }

    @PostMapping("/create-admin")
    public String createAdmin(@RequestBody User adminUser) {
        String normalizedEmail = normalizeEmail(adminUser.getEmail());
        if (!isAllowedAdminEmail(normalizedEmail)) {
            return "Only approved admin emails can register";
        }

        String hashedPassword = PasswordUtil.encrypt(adminUser.getPassword());
        User existing = userRepository.findByEmail(normalizedEmail);
        if (existing != null) {
            existing.setEmail(normalizedEmail);
            existing.setPassword(hashedPassword);
            existing.setRole("ADMIN");
            userRepository.save(existing);
            return "Admin already exists, password updated";
        } else {
            adminUser.setEmail(normalizedEmail);
            adminUser.setPassword(hashedPassword);
            adminUser.setRole("ADMIN");
            userRepository.save(adminUser);
            return "Admin created";
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginUser) {
        String normalizedEmail = normalizeEmail(loginUser.getEmail());
        if (!isAllowedAdminEmail(normalizedEmail)) {
            return "Access denied";
        }

        User dbUser = userRepository.findByEmail(normalizedEmail);
        if (dbUser == null) {
            return "Admin Not Found";
        }

        String encryptedPassword = PasswordUtil.encrypt(loginUser.getPassword());
        if (dbUser.getPassword().equals(encryptedPassword)
                && "ADMIN".equals(dbUser.getRole())) {
            return "Admin Login Success";
        }
        return "Invalid Credentials";
    }

    // ✅ ADMIN: VIEW ALL USERS
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ ADMIN: UPDATE USER DETAILS (email/role/password optional)
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updated) {
        Optional<User> existingOpt = userRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }

        User existing = existingOpt.get();
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            String normalizedEmail = normalizeEmail(updated.getEmail());
            if ("ADMIN".equals(existing.getRole()) && !isAllowedAdminEmail(normalizedEmail)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved emails can remain admins");
            }
            existing.setEmail(normalizedEmail);
        }
        if (updated.getRole() != null && !updated.getRole().isBlank()) {
            if ("ADMIN".equalsIgnoreCase(updated.getRole()) && !isAllowedAdminEmail(existing.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved emails can be admins");
            }
            existing.setRole(updated.getRole());
        }
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(PasswordUtil.encrypt(updated.getPassword()));
        }
        return userRepository.save(existing);
    }

    // ✅ ADMIN: DELETE USER
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return "User not found";
        }
        userRepository.deleteById(id);
        return "User deleted";
    }
}