package com.noisevisionsoftware.vitema.utils.excelParser.controller;

import com.noisevisionsoftware.vitema.utils.excelParser.config.ExcelParserConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/diets/parser-settings")
@RequiredArgsConstructor
public class ExcelParserSettingsController {

    private final ExcelParserConfig excelParserConfig;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getParserSettings() {
        return ResponseEntity.ok(Map.of(
                "skipColumnsCount", excelParserConfig.getSkipColumnsCount(),
                "maxSkipColumnsCount", excelParserConfig.getMaxSkipColumnsCount()
        ));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateSkipColumnsCount(@RequestBody Map<String, Integer> request) {
        Integer skipColumnsCount = request.get("skipColumnsCount");

        if (skipColumnsCount == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "skipColumnsCount is required",
                    "currentValue", excelParserConfig.getSkipColumnsCount()
            ));
        }

        if (skipColumnsCount < 0) {
            skipColumnsCount = 0;
        } else if (skipColumnsCount > excelParserConfig.getMaxSkipColumnsCount()) {
            skipColumnsCount = excelParserConfig.getMaxSkipColumnsCount();
        }

        excelParserConfig.setSkipColumnsCount(skipColumnsCount);

        return ResponseEntity.ok(Map.of(
                "skipColumnsCount", excelParserConfig.getSkipColumnsCount(),
                "maxSkipColumnsCount", excelParserConfig.getMaxSkipColumnsCount()
        ));
    }
}
