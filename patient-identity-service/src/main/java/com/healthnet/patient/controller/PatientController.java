package com.healthnet.patient.controller;

import com.healthnet.patient.model.dto.PatientDto;
import com.healthnet.patient.service.PatientIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientIdentityService patientIdentityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('FACILITY_ADMIN', 'CLINICIAN', 'ADMIN')")
    public ResponseEntity<PatientDto.Response> registerPatient(
            @Valid @RequestBody PatientDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientIdentityService.registerPatient(request));
    }

    @GetMapping("/mpi/{mpiId}")
    @PreAuthorize("hasAnyRole('FACILITY_ADMIN', 'CLINICIAN', 'ADMIN', 'PHARMACY', 'LAB')")
    public ResponseEntity<PatientDto.Response> getByMpiId(@PathVariable String mpiId) {
        return ResponseEntity.ok(patientIdentityService.findByMpiId(mpiId));
    }

    @GetMapping("/national/{nationalId}")
    @PreAuthorize("hasAnyRole('FACILITY_ADMIN', 'CLINICIAN', 'ADMIN')")
    public ResponseEntity<PatientDto.Response> getByNationalId(@PathVariable String nationalId) {
        return ResponseEntity.ok(patientIdentityService.findByNationalId(nationalId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('FACILITY_ADMIN', 'CLINICIAN', 'ADMIN')")
    public ResponseEntity<List<PatientDto.Response>> searchPatients(
            @ModelAttribute PatientDto.SearchRequest searchRequest) {
        return ResponseEntity.ok(patientIdentityService.searchPatients(searchRequest));
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('FACILITY_ADMIN', 'CLINICIAN', 'ADMIN')")
    public ResponseEntity<PatientDto.Response> updatePatient(
            @PathVariable UUID patientId,
            @Valid @RequestBody PatientDto.UpdateRequest request) {
        return ResponseEntity.ok(patientIdentityService.updatePatient(patientId, request));
    }

    @DeleteMapping("/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePatient(@PathVariable UUID patientId) {
        patientIdentityService.deactivatePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
