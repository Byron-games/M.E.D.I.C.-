package com.healthnet.telemedicine.controller;

import com.healthnet.telemedicine.model.dto.SessionDto;
import com.healthnet.telemedicine.service.TelemedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/telemedicine")
@RequiredArgsConstructor
public class TelemedicineController {

    private final TelemedicineService telemedicineService;

    @PostMapping("/sessions")
    public ResponseEntity<SessionDto.Response> createSession(
            @Valid @RequestBody SessionDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(telemedicineService.createSession(request));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionDto.Response> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(telemedicineService.getSession(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<SessionDto.Response> startSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(telemedicineService.startSession(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<SessionDto.Response> endSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(telemedicineService.endSession(sessionId));
    }
}
