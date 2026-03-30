package com.healthnet.emr.controller;

import com.healthnet.emr.model.entity.MedicalRecord;
import com.healthnet.emr.service.EmrService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class EmrController {

    private final EmrService emrService;

    @PostMapping
    public ResponseEntity<MedicalRecord> create(@RequestBody MedicalRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emrService.createRecord(record));
    }

    @GetMapping("/patient/{mpiId}")
    public ResponseEntity<Page<MedicalRecord>> getHistory(
            @PathVariable String mpiId,
            @PageableDefault(size = 20, sort = "visitDate") Pageable pageable) {
        return ResponseEntity.ok(emrService.getPatientHistory(mpiId, pageable));
    }

    @GetMapping("/patient/{mpiId}/range")
    public ResponseEntity<List<MedicalRecord>> getHistoryInRange(
            @PathVariable String mpiId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(emrService.getPatientHistoryInRange(mpiId, from, to));
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<MedicalRecord> getRecord(@PathVariable UUID recordId) {
        return ResponseEntity.ok(emrService.getRecord(recordId));
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<MedicalRecord> updateRecord(
            @PathVariable UUID recordId, @RequestBody MedicalRecord updates) {
        return ResponseEntity.ok(emrService.updateRecord(recordId, updates));
    }

    @PostMapping("/{recordId}/share")
    public ResponseEntity<Void> shareToNetwork(@PathVariable UUID recordId) {
        emrService.shareToNetwork(recordId);
        return ResponseEntity.noContent().build();
    }
}
