package com.backend.backend.dtos;

import com.backend.backend.models.TechStack;
import com.backend.backend.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<TechStackDto> techStacks;

    public static UserResponse from(User user) {
        Set<TechStackDto> techDtos = user.getTechStacks().stream()
                .map(TechStackDto::from)
                .collect(Collectors.toSet());
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                techDtos
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechStackDto {
        private Long id;
        private String name;
        private String type;

        public static TechStackDto from(TechStack t) {
            return new TechStackDto(t.getId(), t.getName(), t.getType().name());
        }
    }
}
