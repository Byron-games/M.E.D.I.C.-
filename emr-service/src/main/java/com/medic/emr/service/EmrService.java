package com.medic.emr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.emr.model.entity.MedicalRecord;
import com.medic.emr.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmrService {

    private final MedicalRecordRepository recordRepository;
    private final ObjectMapper objectMapper;

    public MedicalRecord createRecord(MedicalRecord record) {
        log.info("Creating {} record for patient MPI: {}", record.getRecordType(), record.getMpiId());
        MedicalRecord saved = recordRepository.save(record);
        log.info("Saved EMR record: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecord> getPatientHistory(String mpiId, Pageable pageable) {
        return recordRepository.findByMpiIdOrderByVisitDateDesc(mpiId, pageable);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getPatientHistoryInRange(String mpiId,
                                                         LocalDateTime from,
                                                         LocalDateTime to) {
        return recordRepository.findByMpiIdAndDateRange(mpiId, from, to);
    }

    @Transactional(readOnly = true)
    public MedicalRecord getRecord(UUID id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found: " + id));
    }

    public MedicalRecord updateRecord(UUID id, MedicalRecord updates) {
        MedicalRecord existing = getRecord(id);
        // Only allow updating clinical content — never patient identity or facility
        existing.setClinicalNotes(updates.getClinicalNotes());
        existing.setDiagnosisCodes(updates.getDiagnosisCodes());
        existing.setTreatments(updates.getTreatments());
        existing.setLabResults(updates.getLabResults());
        existing.setVitalSigns(updates.getVitalSigns());
        existing.setFollowUpInstructions(updates.getFollowUpInstructions());
        return recordRepository.save(existing);
    }

    public void shareToNetwork(UUID id) {
        MedicalRecord record = getRecord(id);
        record.setSharedToNetwork(true);
        recordRepository.save(record);
        log.info("Record {} shared to national health network", id);
    }
}
