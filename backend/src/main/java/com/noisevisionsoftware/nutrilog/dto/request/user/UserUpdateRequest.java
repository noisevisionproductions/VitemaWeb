package com.noisevisionsoftware.nutrilog.dto.request.user;

import com.noisevisionsoftware.nutrilog.model.user.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private String nickname;
    private Gender gender;
    private Long birthDate;
    private String note;
}