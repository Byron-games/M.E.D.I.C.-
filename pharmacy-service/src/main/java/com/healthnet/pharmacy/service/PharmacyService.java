package com.healthnet.pharmacy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthnet.pharmacy.model.dto.PrescriptionDto;
import com.healthnet.pharmacy.model.entity.Prescription;
import com.healthnet.pharmacy.model.entity.Prescription.PrescriptionStatus;
import com.healthnet.pharmacy.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PharmacyService {

    private final PrescriptionRepository prescriptionRepository;
    private final DrugInteractionChecker interactionChecker;
    private final ObjectMapper objectMapper;

    public PrescriptionDto.Response issuePrescription(PrescriptionDto.CreateRequest request) {
        log.info("Issuing prescription for patient MPI: {}", request.getMpiId());

        // Check for drug interactions before issuing
        List<PrescriptionDto.InteractionWarning> warnings =
                interactionChecker.checkInteractions(request.getDrugs());

        if (!warnings.isEmpty()) {
            log.warn("Drug interaction warnings for patient {}: {} interactions found",
                    request.getMpiId(), warnings.size());
        }

        Prescription prescription = Prescription.builder()
                .mpiId(request.getMpiId())
                .prescriberId(request.getPrescriberId())
                .prescriberName(request.getPrescriberName())
                .prescriberLicenseNo(request.getPrescriberLicenseNo())
                .issuingFacilityId(request.getIssuingFacilityId())
                .pharmacyId(request.getPharmacyId())
                .pharmacyName(request.getPharmacyName())
                .drugs(toJson(request.getDrugs()))
                .interactionWarnings(warnings.isEmpty() ? null : toJson(warnings))
                .status(PrescriptionStatus.ISSUED)
                .expiryDate(request.getExpiryDate())
                .notes(request.getNotes())
                .build();

        prescription = prescriptionRepository.save(prescription);
        log.info("Issued prescription: {} (RX code: {})", prescription.getId(), prescription.getRxCode());
        return toResponse(prescription);
    }

    public PrescriptionDto.Response sendToPharmacy(UUID prescriptionId, String pharmacyId, String pharmacyName) {
        Prescription p = findOrThrow(prescriptionId);
        if (p.getStatus() != PrescriptionStatus.ISSUED) {
            throw new IllegalStateException("Prescription " + prescriptionId + " is not in ISSUED status");
        }
        p.setPharmacyId(pharmacyId);
        p.setPharmacyName(pharmacyName);
        p.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        return toResponse(prescriptionRepository.save(p));
    }

    public PrescriptionDto.Response dispense(String rxCode) {
        Prescription p = prescriptionRepository.findByRxCode(rxCode)
                .orElseThrow(() -> new RuntimeException("Prescription not found with RX code: " + rxCode));

        if (p.getStatus() == PrescriptionStatus.DISPENSED) {
            throw new IllegalStateException("Prescription " + rxCode + " has already been dispensed");
        }
        if (p.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalStateException("Prescription " + rxCode + " has expired");
        }

        p.setStatus(PrescriptionStatus.DISPENSED);
        p.setDispensedAt(LocalDateTime.now());
        log.info("Dispensed prescription: {}", rxCode);
        return toResponse(prescriptionRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto.Response> getPatientPrescriptions(String mpiId) {
        return prescriptionRepository.findByMpiIdOrderByCreatedAtDesc(mpiId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto.Response> getPendingForPharmacy(String pharmacyId) {
        return prescriptionRepository.findByPharmacyIdAndStatus(pharmacyId, PrescriptionStatus.SENT_TO_PHARMACY)
                .stream().map(this::toResponse).toList();
    }

    private Prescription findOrThrow(UUID id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + id));
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { throw new RuntimeException("JSON serialization failed", e); }
    }

    private <T> List<T> fromJson(String json, TypeReference<List<T>> ref) {
        if (json == null) return new ArrayList<>();
        try { return objectMapper.readValue(json, ref); }
        catch (Exception e) { return new ArrayList<>(); }
    }

    private PrescriptionDto.Response toResponse(Prescription p) {
        return PrescriptionDto.Response.builder()
                .id(p.getId())
                .rxCode(p.getRxCode())
                .mpiId(p.getMpiId())
                .prescriberId(p.getPrescriberId())
                .prescriberName(p.getPrescriberName())
                .issuingFacilityId(p.getIssuingFacilityId())
                .pharmacyId(p.getPharmacyId())
                .pharmacyName(p.getPharmacyName())
                .drugs(fromJson(p.getDrugs(), new TypeReference<>() {}))
                .interactionWarnings(fromJson(p.getInteractionWarnings(), new TypeReference<>() {}))
                .status(p.getStatus())
                .expiryDate(p.getExpiryDate())
                .notes(p.getNotes())
                .dispensedAt(p.getDispensedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
