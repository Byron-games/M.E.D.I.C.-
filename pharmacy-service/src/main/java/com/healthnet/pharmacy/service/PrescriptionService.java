package com.healthnet.pharmacy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthnet.pharmacy.model.dto.PrescriptionDto;
import com.healthnet.pharmacy.model.entity.Prescription;
import com.healthnet.pharmacy.model.entity.Prescription.PrescriptionStatus;
import com.healthnet.pharmacy.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final DrugInteractionService drugInteractionService;
    private final ObjectMapper objectMapper;

    public PrescriptionDto.Response issuePrescription(PrescriptionDto.CreateRequest request) {
        log.info("Issuing prescription for patient {} by clinician {}", request.getMpiId(), request.getPrescriberId());

        // Run drug interaction check before saving
        List<Map<String, String>> warnings = drugInteractionService.checkInteractions(request.getDrugsJson());

        String warningsJson = null;
        if (!warnings.isEmpty()) {
            try {
                warningsJson = objectMapper.writeValueAsString(warnings);
                log.warn("Prescription for {} has {} drug interaction warning(s)", request.getMpiId(), warnings.size());
            } catch (Exception e) {
                log.error("Failed to serialize interaction warnings", e);
            }
        }

        Prescription prescription = Prescription.builder()
                .mpiId(request.getMpiId())
                .prescriberId(request.getPrescriberId())
                .prescriberName(request.getPrescriberName())
                .prescriberLicenseNo(request.getPrescriberLicenseNo())
                .issuingFacilityId(request.getIssuingFacilityId())
                .pharmacyId(request.getPharmacyId())
                .pharmacyName(request.getPharmacyName())
                .drugs(request.getDrugsJson())
                .interactionWarnings(warningsJson)
                .status(PrescriptionStatus.ISSUED)
                .expiryDate(LocalDate.now().plusDays(30))
                .notes(request.getNotes())
                .build();

        prescription = prescriptionRepository.save(prescription);
        log.info("Issued prescription: {}", prescription.getRxCode());
        return toResponse(prescription, warnings);
    }

    public PrescriptionDto.Response dispense(String rxCode, String pharmacyId) {
        Prescription prescription = prescriptionRepository.findByRxCode(rxCode)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + rxCode));

        if (prescription.getStatus() != PrescriptionStatus.ISSUED &&
            prescription.getStatus() != PrescriptionStatus.SENT_TO_PHARMACY) {
            throw new IllegalStateException("Prescription " + rxCode + " cannot be dispensed. Status: " + prescription.getStatus());
        }

        if (prescription.getExpiryDate().isBefore(LocalDate.now())) {
            prescription.setStatus(PrescriptionStatus.EXPIRED);
            prescriptionRepository.save(prescription);
            throw new IllegalStateException("Prescription " + rxCode + " has expired.");
        }

        prescription.setStatus(PrescriptionStatus.DISPENSED);
        prescription.setPharmacyId(pharmacyId);
        prescription.setDispensedAt(LocalDateTime.now());
        prescription = prescriptionRepository.save(prescription);

        log.info("Dispensed prescription {} at pharmacy {}", rxCode, pharmacyId);
        return toResponse(prescription, List.of());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto.Response> getPatientPrescriptions(String mpiId) {
        return prescriptionRepository.findByMpiIdOrderByCreatedAtDesc(mpiId)
                .stream().map(p -> toResponse(p, List.of())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrescriptionDto.Response getByRxCode(String rxCode) {
        return prescriptionRepository.findByRxCode(rxCode)
                .map(p -> toResponse(p, List.of()))
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + rxCode));
    }

    private PrescriptionDto.Response toResponse(Prescription p, List<Map<String, String>> warnings) {
        return PrescriptionDto.Response.builder()
                .id(p.getId())
                .rxCode(p.getRxCode())
                .mpiId(p.getMpiId())
                .prescriberId(p.getPrescriberId())
                .prescriberName(p.getPrescriberName())
                .pharmacyId(p.getPharmacyId())
                .pharmacyName(p.getPharmacyName())
                .drugsJson(p.getDrugs())
                .interactionWarnings(warnings)
                .status(p.getStatus())
                .expiryDate(p.getExpiryDate())
                .notes(p.getNotes())
                .dispensedAt(p.getDispensedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
