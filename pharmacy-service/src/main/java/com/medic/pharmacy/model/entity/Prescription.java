package com.medic.pharmacy.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescriptions", indexes = {
    @Index(name = "idx_rx_mpi_id", columnList = "mpiId"),
    @Index(name = "idx_rx_status", columnList = "status"),
    @Index(name = "idx_rx_pharmacy", columnList = "pharmacyId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unique e-prescription code scannable at the pharmacy counter */
    @Column(nullable = false, unique = true)
    private String rxCode;

    @Column(nullable = false)
    private String mpiId;

    @Column(nullable = false)
    private String prescriberId;

    private String prescriberName;
    private String prescriberLicenseNo;

    /** The facility where the prescription was issued */
    @Column(nullable = false)
    private String issuingFacilityId;

    /** Target pharmacy (null = patient chooses) */
    private String pharmacyId;
    private String pharmacyName;

    /** Prescribed drugs as a JSON array of {drugName, dosage, frequency, duration, notes} */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String drugs;

    /** Drug interaction warnings as JSON (populated by interaction checker) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String interactionWarnings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(length = 500)
    private String notes;

    private LocalDateTime dispensedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateRxCode() {
        if (this.rxCode == null) {
            this.rxCode = "RX-" + UUID.randomUUID().toString().toUpperCase()
                    .replace("-", "").substring(0, 10);
        }
    }

    public enum PrescriptionStatus {
        ISSUED, SENT_TO_PHARMACY, DISPENSED, EXPIRED, CANCELLED
    }
}
