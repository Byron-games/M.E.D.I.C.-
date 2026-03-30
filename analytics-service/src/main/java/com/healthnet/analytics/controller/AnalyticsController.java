package com.healthnet.analytics.controller;

import com.healthnet.analytics.model.dto.AnalyticsDto;
import com.healthnet.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/alerts/outbreaks")
    public ResponseEntity<List<AnalyticsDto.DiseaseReport>> getOutbreakAlerts() {
        return ResponseEntity.ok(analyticsService.getOutbreakAlerts());
    }

    @GetMapping("/disease/region/{region}")
    public ResponseEntity<List<AnalyticsDto.DiseaseReport>> getByRegion(
            @PathVariable String region,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getDiseaseByRegion(region, from, to));
    }

    @GetMapping("/disease/icd/{icdCode}")
    public ResponseEntity<List<AnalyticsDto.DiseaseReport>> getByIcdCode(
            @PathVariable String icdCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getDiseaseByIcdCode(icdCode, from, to));
    }
}
