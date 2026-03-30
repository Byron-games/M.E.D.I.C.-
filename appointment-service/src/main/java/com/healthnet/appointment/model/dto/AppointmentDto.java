package com.healthnet.appointment.model.dto;

import com.healthnet.appointment.model.entity.Appointment.AppointmentStatus;
import com.healthnet.appointment.model.entity.Appointment.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank private String mpiId;
        @NotBlank private String patientName;
        @NotBlank private String facilityId;
        private String facilityName;
        @NotBlank private String clinicianId;
        private String clinicianName;
        private String clinicianSpecialty;
        @NotNull private AppointmentType type;
        @NotNull @Future private LocalDateTime scheduledAt;
        @Min(5) @Max(480) private Integer durationMinutes;
        @Size(max = 500) private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private String mpiId;
        private String patientName;
        private String facilityId;
        private String facilityName;
        private String clinicianId;
        private String clinicianName;
        private String clinicianSpecialty;
        private AppointmentType type;
        private AppointmentStatus status;
        private LocalDateTime scheduledAt;
        private Integer durationMinutes;
        private String notes;
        private String telemedicineJoinUrl;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateStatusRequest {
        @NotNull private AppointmentStatus status;
        private String cancellationReason;
    }
}
