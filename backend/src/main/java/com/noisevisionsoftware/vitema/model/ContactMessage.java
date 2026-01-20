package com.noisevisionsoftware.vitema.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String message;
    private Timestamp createdAt;
    private String status;
}
