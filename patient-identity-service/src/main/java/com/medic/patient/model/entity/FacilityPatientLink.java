package com.medic.patient.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Links a patient to the facilities that have registered or accessed their record.
 * Critical for the Master Patient Index (MPI) cross-facility linkage.
 */
@Entity
@Table(name = "facility_patient_links",
       uniqueConstraints = @UniqueConstraint(columnNames = {"patientId", "facilityId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityPatientLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    /** The facility's own local patient ID for cross-reference */
    private String localPatientId;

    @Column(nullable = false)
    private String facilityId;

    private String facilityName;

    @Enumerated(EnumType.STRING)
    private FacilityType facilityType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime linkedAt;

    public enum FacilityType {
        GOVERNMENT_CLINIC, PRIVATE_HOSPITAL, PHARMACY, LABORATORY, TELEMEDICINE_PROVIDER
    }
}
