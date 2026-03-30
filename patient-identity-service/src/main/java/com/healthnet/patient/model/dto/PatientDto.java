package com.healthnet.patient.model.dto;

import com.healthnet.patient.model.entity.Patient.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PatientDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "National ID is required")
        private String nationalId;

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100)
        private String lastName;

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        private LocalDate dateOfBirth;

        @NotNull(message = "Gender is required")
        private Gender gender;

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
        private String phoneNumber;

        @Email(message = "Invalid email address")
        private String email;

        private String address;
        private String region;
        private String bloodType;
        private String knownAllergies;

        // The facility registering this patient
        @NotBlank
        private String facilityId;
        private String facilityName;
        private String localPatientId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String mpiId;
        private String nationalId;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private Gender gender;
        private String phoneNumber;
        private String email;
        private String address;
        private String region;
        private String bloodType;
        private String knownAllergies;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String nationalId;
        private String mpiId;
        private String phoneNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String phoneNumber;
        private String email;
        private String address;
        private String region;
        private String knownAllergies;
    }
}
