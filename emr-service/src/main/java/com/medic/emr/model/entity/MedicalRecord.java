package com.medic.emr.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_records", indexes = {
    @Index(name = "idx_record_mpi_id", columnList = "mpiId"),
    @Index(name = "idx_record_facility", columnList = "facilityId"),
    @Index(name = "idx_record_date", columnList = "visitDate")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Links back to the Patient Identity Service */
    @Column(nullable = false)
    private String mpiId;

    @Column(nullable = false)
    private String facilityId;

    private String facilityName;

    @Column(nullable = false)
    private String attendingClinicianId;

    private String attendingClinicianName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType recordType;

    @Column(nullable = false)
    private LocalDateTime visitDate;

    /** Chief complaint in plain text */
    @Column(length = 1000)
    private String chiefComplaint;

    /** Structured clinical notes as JSON (SOAP format) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String clinicalNotes;

    /** ICD-10 diagnosis codes as JSON array */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String diagnosisCodes;

    /** Prescribed treatments as JSON */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String treatments;

    /** Lab results as JSON */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String labResults;

    /** Vital signs (BP, temp, weight, etc.) as JSON */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String vitalSigns;

    @Column(length = 500)
    private String followUpInstructions;

    /** Whether this record is shared to the national network */
    private boolean sharedToNetwork = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum RecordType {
        OUTPATIENT_VISIT,
        INPATIENT_ADMISSION,
        EMERGENCY,
        TELEMEDICINE_CONSULTATION,
        LAB_RESULT,
        RADIOLOGY,
        SURGICAL,
        VACCINATION,
        PRESCRIPTION
    }
}
