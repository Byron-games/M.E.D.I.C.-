package com.healthnet.appointment.controller;

import com.healthnet.appointment.model.dto.AppointmentDto;
import com.healthnet.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentDto.Response> create(
            @Valid @RequestBody AppointmentDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(request));
    }

    @GetMapping("/patient/{mpiId}")
    public ResponseEntity<List<AppointmentDto.Response>> getPatientAppointments(
            @PathVariable String mpiId) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(mpiId));
    }

    @GetMapping("/clinician/{clinicianId}/schedule")
    public ResponseEntity<List<AppointmentDto.Response>> getClinicianSchedule(
            @PathVariable String clinicianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(appointmentService.getClinicianSchedule(clinicianId, from, to));
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentDto.Response> updateStatus(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AppointmentDto.UpdateStatusRequest request) {
        return ResponseEntity.ok(appointmentService.updateStatus(appointmentId, request));
    }
}
