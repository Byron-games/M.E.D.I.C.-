package com.medic.analytics.model.dto;

import lombok.*;
import java.time.LocalDate;

public class AnalyticsDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DiseaseReport {
        private LocalDate snapshotDate;
        private String icdCode;
        private String icdDescription;
        private String region;
        private Long caseCount;
        private Long newCasesVsPreviousWeek;
        private boolean outbreakAlert;
        private String alertLevel;
    }
}
