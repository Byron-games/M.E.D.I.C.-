package com.healthnet.telemedicine.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "telemedicine_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TelemedicineSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID appointmentId;

    @Column(nullable = false)
    private String mpiId;

    @Column(nullable = false)
    private String clinicianId;

    /** External video provider session ID (e.g., Daily.co, Jitsi room name) */
    @Column(nullable = false, unique = true)
    private String providerSessionId;

    /** Patient join URL */
    @Column(nullable = false, length = 500)
    private String patientJoinUrl;

    /** Clinician join URL (contains host token) */
    @Column(nullable = false, length = 500)
    private String clinicianJoinUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    /** Duration in seconds for billing/analytics */
    private Long durationSeconds;

    /** Whether low-bandwidth mode is enabled (for rural areas) */
    private boolean lowBandwidthMode = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum SessionStatus {
        CREATED, ACTIVE, COMPLETED, EXPIRED, CANCELLED
    }
}
