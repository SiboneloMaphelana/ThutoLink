package com.backend.backend.services;

import com.backend.backend.dtos.OnboardingTechStackRequest;
import com.backend.backend.models.TechStack;
import com.backend.backend.models.User;
import com.backend.backend.repositories.TechStackRepository;
import com.backend.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TechStackRepository techStackRepository;

    /**
     * Adds tech stacks to the user's profile (onboarding). Finds or creates each tech stack by name.
     */
    @Transactional
    public User addTechStacks(String userEmail, OnboardingTechStackRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<TechStack> toAdd = new ArrayList<>();
        for (OnboardingTechStackRequest.TechStackItemDto item : request.getTechStacks()) {
            TechStack tech = techStackRepository.findByName(item.getName())
                    .orElseGet(() -> {
                        TechStack newTech = new TechStack();
                        newTech.setName(item.getName());
                        newTech.setType(item.getType());
                        return techStackRepository.save(newTech);
                    });
            toAdd.add(tech);
        }

        user.getTechStacks().addAll(toAdd);
        return userRepository.save(user);
    }

    /**
     * Returns the user for the given email, if present.
     */
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
