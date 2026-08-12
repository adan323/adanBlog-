package com.adan.blog.controller;

import com.adan.blog.dto.LoginRequest;
import com.adan.blog.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        String username = (String) authentication.getPrincipal();
        authService.changePassword(username, body.get("oldPassword"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
