package com.noisevisionsoftware.vitema.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
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
    private String trainerId;
}