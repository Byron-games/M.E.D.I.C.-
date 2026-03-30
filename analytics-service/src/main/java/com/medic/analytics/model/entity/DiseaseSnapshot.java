package com.medic.analytics.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disease_snapshots", indexes = {
    @Index(name = "idx_snap_date", columnList = "snapshotDate"),
    @Index(name = "idx_snap_icd", columnList = "icdCode"),
    @Index(name = "idx_snap_region", columnList = "region")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiseaseSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    /** ICD-10 code for the disease/condition */
    @Column(nullable = false)
    private String icdCode;

    private String icdDescription;

    /** Geographic region - ANONYMIZED, no patient identifiers */
    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private Long caseCount;

    private Long newCasesVsPreviousWeek;

    /** Outbreak alert triggered if case count exceeds threshold */
    private boolean outbreakAlert;

    private String alertLevel; // GREEN, YELLOW, ORANGE, RED

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
