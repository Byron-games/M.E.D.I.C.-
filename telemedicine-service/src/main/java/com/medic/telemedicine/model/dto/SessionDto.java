package com.medic.telemedicine.model.dto;

import com.medic.telemedicine.model.entity.TelemedicineSession.SessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class SessionDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotNull  private UUID appointmentId;
        @NotBlank private String mpiId;
        @NotBlank private String clinicianId;
        private LocalDateTime scheduledAt;
        private boolean lowBandwidthMode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private UUID id;
        private UUID appointmentId;
        private String mpiId;
        private String clinicianId;
        private String patientJoinUrl;
        private String clinicianJoinUrl;
        private SessionStatus status;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private Long durationSeconds;
        private boolean lowBandwidthMode;
        private LocalDateTime createdAt;
    }
}
