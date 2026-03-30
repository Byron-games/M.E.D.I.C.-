package com.healthnet.analytics.service;

import com.healthnet.analytics.model.dto.AnalyticsDto;
import com.healthnet.analytics.model.entity.DiseaseSnapshot;
import com.healthnet.analytics.repository.DiseaseSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsService {

    private final DiseaseSnapshotRepository snapshotRepository;

    /** Outbreak threshold: cases per region in 7 days */
    private static final Map<String, Long> OUTBREAK_THRESHOLDS = Map.of(
        "GREEN",  10L,
        "YELLOW", 25L,
        "ORANGE", 50L,
        "RED",    100L
    );

    /**
     * Scheduled job: runs every day at 02:00 AM.
     * Pulls anonymized diagnosis data from EMR service and detects outbreak patterns.
     * NO patient identifiers are stored — only aggregate counts by region + ICD code.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyOutbreakDetection() {
        log.info("Running daily outbreak detection job...");
        // In production: call EMR service aggregate endpoint
        // For scaffold: log and return
        log.info("Outbreak detection job completed");
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDto.DiseaseReport> getOutbreakAlerts() {
        return snapshotRepository.findByOutbreakAlertTrueAndSnapshotDateAfter(
                LocalDate.now().minusDays(7))
            .stream()
            .map(this::toReport)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDto.DiseaseReport> getDiseaseByRegion(String region, LocalDate from, LocalDate to) {
        return snapshotRepository.findByRegionAndSnapshotDateBetween(region, from, to)
            .stream()
            .map(this::toReport)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDto.DiseaseReport> getDiseaseByIcdCode(String icdCode, LocalDate from, LocalDate to) {
        return snapshotRepository.findByIcdCodeAndSnapshotDateBetween(icdCode, from, to)
            .stream()
            .map(this::toReport)
            .collect(Collectors.toList());
    }

    private String determineAlertLevel(long caseCount) {
        if (caseCount >= OUTBREAK_THRESHOLDS.get("RED"))    return "RED";
        if (caseCount >= OUTBREAK_THRESHOLDS.get("ORANGE")) return "ORANGE";
        if (caseCount >= OUTBREAK_THRESHOLDS.get("YELLOW")) return "YELLOW";
        return "GREEN";
    }

    private AnalyticsDto.DiseaseReport toReport(DiseaseSnapshot s) {
        return AnalyticsDto.DiseaseReport.builder()
                .snapshotDate(s.getSnapshotDate())
                .icdCode(s.getIcdCode())
                .icdDescription(s.getIcdDescription())
                .region(s.getRegion())
                .caseCount(s.getCaseCount())
                .newCasesVsPreviousWeek(s.getNewCasesVsPreviousWeek())
                .outbreakAlert(s.isOutbreakAlert())
                .alertLevel(s.getAlertLevel())
                .build();
    }
}
