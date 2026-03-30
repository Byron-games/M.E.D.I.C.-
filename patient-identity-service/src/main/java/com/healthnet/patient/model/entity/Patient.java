package com.healthnet.patient.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_national_id", columnList = "nationalId", unique = true),
    @Index(name = "idx_patient_name_dob", columnList = "firstName, lastName, dateOfBirth")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Master Patient Index - unique global identifier */
    @Column(nullable = false, unique = true, updatable = false)
    private String mpiId;

    @Column(nullable = false)
    private String nationalId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private String phoneNumber;
    private String email;
    private String address;
    private String region;

    /** Blood type, allergies summary — not full EMR */
    private String bloodType;

    @Column(length = 1000)
    private String knownAllergies;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateMpiId() {
        if (this.mpiId == null) {
            this.mpiId = "MPI-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 12);
        }
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
