package com.backend.backend.dtos;

import com.backend.backend.models.TechType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingTechStackRequest {

    @NotEmpty(message = "At least one tech stack item is required")
    @Valid
    private List<TechStackItemDto> techStacks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechStackItemDto {
        @jakarta.validation.constraints.NotBlank(message = "Tech name is required")
        private String name;
        @jakarta.validation.constraints.NotNull(message = "Tech type is required")
        private TechType type;
    }
}
