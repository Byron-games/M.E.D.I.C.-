package com.healthnet.pharmacy.controller;

import com.healthnet.pharmacy.model.dto.PrescriptionDto;
import com.healthnet.pharmacy.service.PharmacyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @PostMapping("/prescriptions")
    public ResponseEntity<PrescriptionDto.Response> issue(
            @Valid @RequestBody PrescriptionDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pharmacyService.issuePrescription(request));
    }

    @GetMapping("/prescriptions/patient/{mpiId}")
    public ResponseEntity<List<PrescriptionDto.Response>> getPatientPrescriptions(
            @PathVariable String mpiId) {
        return ResponseEntity.ok(pharmacyService.getPatientPrescriptions(mpiId));
    }

    @GetMapping("/prescriptions/pending")
    public ResponseEntity<List<PrescriptionDto.Response>> getPending(
            @RequestParam String pharmacyId) {
        return ResponseEntity.ok(pharmacyService.getPendingForPharmacy(pharmacyId));
    }

    @PostMapping("/prescriptions/{id}/send")
    public ResponseEntity<PrescriptionDto.Response> sendToPharmacy(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                pharmacyService.sendToPharmacy(id, body.get("pharmacyId"), body.get("pharmacyName")));
    }

    @PostMapping("/prescriptions/dispense")
    public ResponseEntity<PrescriptionDto.Response> dispense(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(pharmacyService.dispense(body.get("rxCode")));
    }
}
