package com.noisevisionsoftware.vitema.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseUser {
    private String uid;
    private String email;
    private String role;
    private String displayName;
}