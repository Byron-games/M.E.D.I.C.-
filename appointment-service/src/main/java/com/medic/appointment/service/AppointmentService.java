package com.medic.appointment.service;

import com.medic.appointment.model.dto.AppointmentDto;
import com.medic.appointment.model.entity.Appointment;
import com.medic.appointment.model.entity.Appointment.AppointmentStatus;
import com.medic.appointment.model.entity.Appointment.AppointmentType;
import com.medic.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentDto.Response createAppointment(AppointmentDto.CreateRequest request) {
        log.info("Creating {} appointment for patient {} with clinician {}",
                request.getType(), request.getMpiId(), request.getClinicianId());

        // Check for scheduling conflicts
        LocalDateTime end = request.getScheduledAt()
                .plusMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);

        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                request.getClinicianId(), request.getScheduledAt(), end);

        if (!conflicts.isEmpty()) {
            throw new IllegalStateException(
                    "Clinician " + request.getClinicianId() + " has a scheduling conflict at " +
                    request.getScheduledAt());
        }

        Appointment appointment = Appointment.builder()
                .mpiId(request.getMpiId())
                .patientName(request.getPatientName())
                .facilityId(request.getFacilityId())
                .facilityName(request.getFacilityName())
                .clinicianId(request.getClinicianId())
                .clinicianName(request.getClinicianName())
                .clinicianSpecialty(request.getClinicianSpecialty())
                .type(request.getType())
                .status(AppointmentStatus.SCHEDULED)
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30)
                .notes(request.getNotes())
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Created appointment: {}", appointment.getId());
        return toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto.Response> getPatientAppointments(String mpiId) {
        return appointmentRepository.findByMpiIdOrderByScheduledAtDesc(mpiId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto.Response> getClinicianSchedule(String clinicianId,
                                                               LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findByClinicianIdAndScheduledAtBetween(clinicianId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AppointmentDto.Response updateStatus(UUID appointmentId,
                                                 AppointmentDto.UpdateStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        appointment.setStatus(request.getStatus());
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(request.getCancellationReason());
        }

        appointment = appointmentRepository.save(appointment);
        log.info("Updated appointment {} status to {}", appointmentId, request.getStatus());
        return toResponse(appointment);
    }

    private AppointmentDto.Response toResponse(Appointment a) {
        return AppointmentDto.Response.builder()
                .id(a.getId())
                .mpiId(a.getMpiId())
                .patientName(a.getPatientName())
                .facilityId(a.getFacilityId())
                .facilityName(a.getFacilityName())
                .clinicianId(a.getClinicianId())
                .clinicianName(a.getClinicianName())
                .clinicianSpecialty(a.getClinicianSpecialty())
                .type(a.getType())
                .status(a.getStatus())
                .scheduledAt(a.getScheduledAt())
                .durationMinutes(a.getDurationMinutes())
                .notes(a.getNotes())
                .telemedicineJoinUrl(a.getTelemedicineJoinUrl())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
