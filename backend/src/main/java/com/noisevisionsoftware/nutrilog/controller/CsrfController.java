package com.noisevisionsoftware.nutrilog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class CsrfController {

    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>> getCsrfToken(CsrfToken token) {
        Map<String, String> response = new HashMap<>();
        if (token != null) {
            response.put("headerName", token.getHeaderName());
            response.put("parameterName", token.getParameterName());
            response.put("token", token.getToken());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.noContent().build();
    }
}