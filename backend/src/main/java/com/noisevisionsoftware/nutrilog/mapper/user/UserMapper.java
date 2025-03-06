package com.noisevisionsoftware.nutrilog.mapper.user;

import com.noisevisionsoftware.nutrilog.dto.request.user.UserUpdateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.UserResponse;
import com.noisevisionsoftware.nutrilog.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .storedAge(user.getStoredAge())
                .profileCompleted(user.isProfileCompleted())
                .role(user.getRole())
                .note(user.getNote())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public void updateUserFromRequest(User user, UserUpdateRequest request) {
        user.setNickname(request.getNickname());
        user.setGender(request.getGender());
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        user.setNote(request.getNote());
    }
}