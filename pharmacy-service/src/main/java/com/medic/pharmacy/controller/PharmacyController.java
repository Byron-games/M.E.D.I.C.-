package com.medic.pharmacy.controller;

import com.medic.pharmacy.model.dto.PrescriptionDto;
import com.medic.pharmacy.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/prescriptions")
    public ResponseEntity<PrescriptionDto.Response> issue(
            @Valid @RequestBody PrescriptionDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.issuePrescription(request));
    }

    @GetMapping("/prescriptions/rx/{rxCode}")
    public ResponseEntity<PrescriptionDto.Response> getByRxCode(@PathVariable String rxCode) {
        return ResponseEntity.ok(prescriptionService.getByRxCode(rxCode));
    }

    @PostMapping("/prescriptions/rx/{rxCode}/dispense")
    public ResponseEntity<PrescriptionDto.Response> dispense(
            @PathVariable String rxCode,
            @RequestHeader("X-User-Id") String pharmacyId) {
        return ResponseEntity.ok(prescriptionService.dispense(rxCode, pharmacyId));
    }

    @GetMapping("/prescriptions/patient/{mpiId}")
    public ResponseEntity<List<PrescriptionDto.Response>> getPatientPrescriptions(
            @PathVariable String mpiId) {
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(mpiId));
    }
}