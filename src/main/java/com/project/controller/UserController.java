package com.project.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.project.entity.User;
import com.project.repository.UserRepository;
import com.project.service.FirebaseAuthService;
import com.project.service.OtpService;
import com.project.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")   // ✅ IMPORTANT
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FirebaseAuthService firebaseAuthService;

    @Autowired
    private OtpService otpService;

    // ✅ REGISTER API
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Please register using OTP verification at /api/auth/verify-otp");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }

    // ✅ LOGIN API
    @PostMapping("/login")
    public String login(@RequestBody User loginUser) {
        try {
            if (loginUser.getEmail() == null || loginUser.getEmail().isBlank() || loginUser.getPassword() == null || loginUser.getPassword().isBlank()) {
                return "Invalid Credentials";
            }

            User dbUser = userRepository.findFirstByEmailOrderByIdAsc(loginUser.getEmail());
            if (dbUser == null) {
                return "User Not Found";
            }

            String storedPassword = dbUser.getPassword();
            if (storedPassword == null || storedPassword.isBlank()) {
                return "Please login with social login";
            }

            String encryptedPassword = PasswordUtil.encrypt(loginUser.getPassword());
            if (encryptedPassword.equals(storedPassword)) {
                return "User Login Success";
            }

            return "Invalid Password";
        } catch (Exception e) {
            e.printStackTrace();
            return "Login Error";
        }
    }

    // ✅ FIREBASE LOGIN API
    @PostMapping("/firebase-login")
    public ResponseEntity<Map<String, Object>> firebaseLogin(@RequestBody FirebaseLoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        if (request.getIdToken() == null || request.getIdToken().isBlank()) {
            response.put("message", "Missing idToken");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> authResult = firebaseAuthService.authenticateWithFirebase(request.getIdToken());
            User user = (User) authResult.get("user");
            boolean isNewUser = (boolean) authResult.get("isNewUser");

            if (!isNewUser && (user.getPassword() == null || user.getPassword().isBlank())) {
                isNewUser = true;
                response.put("message", "Please set a password for your social account.");
            } else {
                response.put("message", "Firebase Login Success");
            }

            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("provider", user.getProvider());
            response.put("role", user.getRole());
            response.put("isNewUser", isNewUser);
            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            response.put("message", "Firebase Login Error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Firebase Login Error");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ SET PASSWORD FOR NEW SOCIAL USERS
    @PostMapping("/set-password")
    public ResponseEntity<Map<String, String>> setPassword(@RequestBody SetPasswordRequest request) {
        Map<String, String> response = new HashMap<>();
        
        if (request.getEmail() == null || request.getEmail().isBlank() || 
            request.getPassword() == null || request.getPassword().isBlank()) {
            response.put("message", "Email and password are required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User user = userRepository.findFirstByEmailOrderByIdAsc(request.getEmail());
            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            user.setPassword(PasswordUtil.encrypt(request.getPassword()));
            userRepository.save(user);
            
            response.put("message", "Password set successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error setting password");
            response.put("error", getRootCauseMessage(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ RESET PASSWORD REQUEST (send OTP to user email)
    @PostMapping("/reset-password-request")
    public ResponseEntity<Map<String, String>> resetPasswordRequest(@RequestBody ResetPasswordRequest request) {
        Map<String, String> response = new HashMap<>();
        
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User user = userRepository.findFirstByEmailOrderByIdAsc(request.getEmail());
            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            otpService.sendOtp(request.getEmail());
            response.put("message", "OTP sent to email");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error sending OTP");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ RESET PASSWORD
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordConfirmRequest request) {
        Map<String, String> response = new HashMap<>();
        
        if (request.getEmail() == null || request.getEmail().isBlank() || 
            request.getNewPassword() == null || request.getNewPassword().isBlank() ||
            request.getOtp() == null || request.getOtp().isBlank()) {
            response.put("message", "Email, OTP, and new password are required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
                response.put("message", "Invalid or expired OTP");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findFirstByEmailOrderByIdAsc(request.getEmail());
            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            user.setPassword(PasswordUtil.encrypt(request.getNewPassword()));
            userRepository.save(user);
            otpService.clearOtp(request.getEmail());
            
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Error resetting password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public static class FirebaseLoginRequest {
        private String idToken;

        public String getIdToken() { return idToken; }
        public void setIdToken(String idToken) { this.idToken = idToken; }
    }

    public static class SetPasswordRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ResetPasswordRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordConfirmRequest {
        private String email;
        private String newPassword;
        private String otp;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }

    private String getRootCauseMessage(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            return cause.getClass().getSimpleName();
        }

        return message;
    }
}
