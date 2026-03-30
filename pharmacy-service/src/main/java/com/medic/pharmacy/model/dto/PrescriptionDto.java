package com.medic.pharmacy.model.dto;

import com.medic.pharmacy.model.entity.Prescription.PrescriptionStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PrescriptionDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank private String mpiId;
        @NotBlank private String prescriberId;
        private String prescriberName;
        private String prescriberLicenseNo;
        @NotBlank private String issuingFacilityId;
        private String pharmacyId;
        private String pharmacyName;
        /** JSON array: [{drugName, dosage, frequency, durationDays, notes}] */
        @NotBlank private String drugs;
        private LocalDate expiryDate;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private String rxCode;
        private String mpiId;
        private String prescriberId;
        private String prescriberName;
        private String issuingFacilityId;
        private String pharmacyId;
        private String pharmacyName;
        private String drugs;
        private List<Map<String, String>> interactionWarnings;
        private PrescriptionStatus status;
        private LocalDate expiryDate;
        private String notes;
        private LocalDateTime dispensedAt;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DrugItem {
        private String drugName;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InteractionWarning {
        private String drug1;
        private String drug2;
        private String severity;
        private String description;
    }
}