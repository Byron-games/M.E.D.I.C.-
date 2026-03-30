package com.healthnet.appointment.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appt_mpi_id", columnList = "mpiId"),
    @Index(name = "idx_appt_clinician", columnList = "clinicianId"),
    @Index(name = "idx_appt_scheduled", columnList = "scheduledAt"),
    @Index(name = "idx_appt_facility", columnList = "facilityId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String mpiId;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String facilityId;

    private String facilityName;

    @Column(nullable = false)
    private String clinicianId;

    private String clinicianName;
    private String clinicianSpecialty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private Integer durationMinutes;

    @Column(length = 500)
    private String notes;

    /** For telemedicine appointments: session join URL */
    private String telemedicineJoinUrl;

    private String cancellationReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AppointmentType {
        IN_PERSON, TELEMEDICINE
    }

    public enum AppointmentStatus {
        SCHEDULED, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    }
}
