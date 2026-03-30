package com.medic.telemedicine.service;

import com.medic.telemedicine.model.dto.SessionDto;
import com.medic.telemedicine.model.entity.TelemedicineSession;
import com.medic.telemedicine.model.entity.TelemedicineSession.SessionStatus;
import com.medic.telemedicine.repository.TelemedicineSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Manages telemedicine video session lifecycle.
 *
 * Integrates with an external video provider (e.g., Daily.co or Jitsi).
 * For low-bandwidth/rural scenarios, generates Jitsi room links which
 * can work on low-spec devices without requiring a plugin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TelemedicineService {

    private final TelemedicineSessionRepository sessionRepository;
    private final VideoProviderClient videoProviderClient;

    @Value("${medic.telemedicine.base-url:https://meet.medic.com}")
    private String telemedicineBaseUrl;

    public SessionDto.Response createSession(SessionDto.CreateRequest request) {
        log.info("Creating telemedicine session for appointment: {}", request.getAppointmentId());

        // Delegate to video provider (Daily.co / Jitsi)
        String roomName = "medic-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        VideoProviderClient.RoomDetails room = videoProviderClient.createRoom(
                roomName, request.isLowBandwidthMode());

        TelemedicineSession session = TelemedicineSession.builder()
                .appointmentId(request.getAppointmentId())
                .mpiId(request.getMpiId())
                .clinicianId(request.getClinicianId())
                .providerSessionId(room.getRoomId())
                .patientJoinUrl(room.getGuestUrl())
                .clinicianJoinUrl(room.getHostUrl())
                .status(SessionStatus.CREATED)
                .scheduledAt(request.getScheduledAt())
                .lowBandwidthMode(request.isLowBandwidthMode())
                .build();

        session = sessionRepository.save(session);
        log.info("Created session {} with room {}", session.getId(), roomName);
        return toResponse(session);
    }

    public SessionDto.Response startSession(UUID sessionId) {
        TelemedicineSession session = findOrThrow(sessionId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        return toResponse(sessionRepository.save(session));
    }

    public SessionDto.Response endSession(UUID sessionId) {
        TelemedicineSession session = findOrThrow(sessionId);
        LocalDateTime now = LocalDateTime.now();
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(now);

        if (session.getStartedAt() != null) {
            session.setDurationSeconds(ChronoUnit.SECONDS.between(session.getStartedAt(), now));
        }

        videoProviderClient.closeRoom(session.getProviderSessionId());
        log.info("Ended session {}, duration: {} seconds", sessionId, session.getDurationSeconds());
        return toResponse(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public SessionDto.Response getSession(UUID sessionId) {
        return toResponse(findOrThrow(sessionId));
    }

    private TelemedicineSession findOrThrow(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    private SessionDto.Response toResponse(TelemedicineSession s) {
        return SessionDto.Response.builder()
                .id(s.getId())
                .appointmentId(s.getAppointmentId())
                .mpiId(s.getMpiId())
                .clinicianId(s.getClinicianId())
                .patientJoinUrl(s.getPatientJoinUrl())
                .clinicianJoinUrl(s.getClinicianJoinUrl())
                .status(s.getStatus())
                .scheduledAt(s.getScheduledAt())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .durationSeconds(s.getDurationSeconds())
                .lowBandwidthMode(s.isLowBandwidthMode())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
