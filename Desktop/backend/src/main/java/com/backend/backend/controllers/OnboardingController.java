package com.backend.backend.controllers;

import com.backend.backend.dtos.OnboardingTechStackRequest;
import com.backend.backend.dtos.UserResponse;
import com.backend.backend.models.User;
import com.backend.backend.security.JwtService;
import com.backend.backend.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Add tech stack for the current user (onboarding). Only allowed with a token
     * issued at registration (sign-up). Login tokens are rejected.
     */
    @PostMapping("/tech-stack")
    public ResponseEntity<?> addTechStack(
            HttpServletRequest request,
            Authentication authentication,
            @Valid @RequestBody OnboardingTechStackRequest body
    ) {
        String token = extractBearerToken(request);
        if (token == null || !jwtService.isNewUserToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Onboarding is only available for newly registered users. Please use the token received after sign-up.");
        }

        String email = authentication.getName();
        User user = userService.addTechStacks(email, body);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }
}
