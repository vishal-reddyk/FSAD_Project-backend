package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.entity.User;
import com.project.repository.UserRepository;
import com.project.service.OtpService;
import com.project.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepository;

    // SEND OTP
    @PostMapping("/send-otp")
    public String sendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null || email.isBlank()) {
            return "Email is required";
        }

        if (userRepository.findFirstByEmailOrderByIdAsc(email) != null) {
            return "Email already registered";
        }

        try {
            otpService.sendOtp(email);
            return "OTP Sent";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending OTP";
        }
    }

    // VERIFY OTP + REGISTER
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String otp = req.get("otp");
        String password = req.get("password");

        if (email == null || email.isBlank() || otp == null || otp.isBlank() || password == null || password.isBlank()) {
            return "Email, password, and OTP are required";
        }

        if (!otpService.verifyOtp(email, otp)) {
            return "Invalid OTP";
        }

        if (userRepository.findFirstByEmailOrderByIdAsc(email) != null) {
            return "User already registered";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setRole("USER");

        userRepository.save(user);
        otpService.clearOtp(email);

        return "Registered Successfully";
    }

    @PostMapping("/social/send-otp")
    public String sendSocialOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String provider = req.get("provider");

        if (email == null || email.isBlank()) {
            return "Email is required";
        }
        if (provider == null || provider.isBlank()) {
            return "Social provider is required";
        }

        try {
            otpService.sendOtp(email);
            return "OTP Sent for " + provider;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending OTP";
        }
    }

    @PostMapping("/social/verify-otp")
    public Map<String, Object> verifySocialOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        String provider = req.get("provider");

        Map<String, Object> response = new HashMap<>();

        if (email == null || email.isBlank() || otp == null || otp.isBlank() || provider == null || provider.isBlank()) {
            response.put("message", "Email, provider and OTP are required");
            response.put("newUser", false);
            return response;
        }

        if (!otpService.verifyOtp(email, otp)) {
            response.put("message", "Invalid OTP");
            response.put("newUser", false);
            return response;
        }

        User user = userRepository.findFirstByEmailOrderByIdAsc(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setRole("USER");
            user.setProvider(provider);
            user.setPassword(null);
            userRepository.save(user);
            otpService.clearOtp(email);

            response.put("message", "Account created with " + provider + ". Please set a password.");
            response.put("newUser", true);
            response.put("email", email);
            response.put("provider", provider);
            return response;
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            if (user.getProvider() == null || user.getProvider().isBlank()) {
                user.setProvider(provider);
                userRepository.save(user);
            }
            otpService.clearOtp(email);

            response.put("message", "Social account exists. Please set a password.");
            response.put("newUser", true);
            response.put("email", email);
            response.put("provider", provider);
            return response;
        }

        otpService.clearOtp(email);
        response.put("message", "Logged in with " + provider);
        response.put("newUser", false);
        response.put("email", email);
        response.put("provider", provider);
        return response;
    }

    @GetMapping("/google")
    public String googleAuth() {
        return "Please use the social login buttons to sign in with OTP.";
    }

    @GetMapping("/github")
    public String githubAuth() {
        return "Please use the social login buttons to sign in with OTP.";
    }
}
