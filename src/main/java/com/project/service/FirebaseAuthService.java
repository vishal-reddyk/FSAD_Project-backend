package com.project.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.project.entity.User;
import com.project.repository.UserRepository;
import com.project.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FirebaseAuthService {

    @Autowired
    private UserRepository userRepository;

    public java.util.Map<String, Object> authenticateWithFirebase(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();
        String email = decodedToken.getEmail();
        String name = decodedToken.getName();
        Object providerClaim = decodedToken.getClaims().get("firebase.sign_in_provider");
        String provider = providerClaim != null ? providerClaim.toString() : null;
        if ("google.com".equals(provider)) {
            provider = "google";
        } else if ("github.com".equals(provider)) {
            provider = "github";
        } else {
            provider = "unknown";
        }

        boolean isNewUser = false;
        User user = userRepository.findByFirebaseUid(uid);
        
        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setFirebaseUid(uid);
            user.setEmail(email);
            user.setName(name);
            user.setProvider(provider);
            user.setRole("USER");
            user.setPassword(null); // Don't set password for new users yet
            userRepository.save(user);
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("user", user);
        result.put("isNewUser", isNewUser);
        return result;
    }
}