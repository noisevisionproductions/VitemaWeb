package com.noisevisionsoftware.nutrilog.dto.response;

import com.noisevisionsoftware.nutrilog.model.user.Gender;
import com.noisevisionsoftware.nutrilog.model.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String nickname;
    private Gender gender;
    private Long birthDate;
    private Integer storedAge;
    private boolean profileCompleted;
    private UserRole role;
    private String note;
    private Long createdAt;
}