package com.noisevisionsoftware.vitema.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfig {

    private String id;
    private String name;
    private String email;
    private String logoUrl;
    private List<String> colors;
    private boolean isDemoAccount;
    private Timestamp createdAt;
    private Timestamp expiresAt;
    private String demoPassword;
    private List<String> demoUsers;
    private List<String> demoDiets;
    private String excelTemplatePath;
    private boolean initialized;
}
