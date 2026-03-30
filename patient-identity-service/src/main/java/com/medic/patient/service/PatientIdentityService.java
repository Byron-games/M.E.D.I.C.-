package com.medic.patient.service;

import com.medic.patient.exception.DuplicatePatientException;
import com.medic.patient.exception.PatientNotFoundException;
import com.medic.patient.mapper.PatientMapper;
import com.medic.patient.model.dto.PatientDto;
import com.medic.patient.model.entity.FacilityPatientLink;
import com.medic.patient.model.entity.Patient;
import com.medic.patient.repository.FacilityPatientLinkRepository;
import com.medic.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientIdentityService {

    private final PatientRepository patientRepository;
    private final FacilityPatientLinkRepository facilityPatientLinkRepository;
    private final PatientMapper patientMapper;
    private final MpiMatchingService mpiMatchingService;

    /**
     * Registers a patient and links them to the originating facility.
     * The MPI matching algorithm runs first to avoid duplicate records.
     */
    public PatientDto.Response registerPatient(PatientDto.CreateRequest request) {
        log.info("Registering new patient with nationalId: {}", request.getNationalId());

        // Step 1: Check for exact national ID match
        if (patientRepository.existsByNationalId(request.getNationalId())) {
            Patient existing = patientRepository.findByNationalId(request.getNationalId())
                    .orElseThrow();
            log.warn("Patient already exists with nationalId: {}. Linking to facility.", request.getNationalId());
            linkPatientToFacility(existing.getId(), request);
            return patientMapper.toResponse(existing);
        }

        // Step 2: Run probabilistic MPI matching to detect potential duplicates
        List<Patient> probableMatches = mpiMatchingService.findProbableMatches(
                request.getFirstName(), request.getLastName(), request.getDateOfBirth());

        if (!probableMatches.isEmpty()) {
            double confidence = mpiMatchingService.calculateMatchConfidence(request, probableMatches.get(0));
            if (confidence >= 0.90) {
                throw new DuplicatePatientException(
                    "High-confidence duplicate patient detected. Existing MPI: " +
                    probableMatches.get(0).getMpiId() +
                    ". Please verify before creating a new record."
                );
            }
            log.warn("Possible duplicate patients found but confidence {} < 90%. Proceeding.", confidence);
        }

        // Step 3: Create new patient
        Patient patient = patientMapper.toEntity(request);
        patient = patientRepository.save(patient);
        log.info("Created new patient with MPI ID: {}", patient.getMpiId());

        // Step 4: Link to facility
        linkPatientToFacility(patient.getId(), request);

        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientDto.Response findByMpiId(String mpiId) {
        return patientRepository.findByMpiId(mpiId)
                .map(patientMapper::toResponse)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with MPI ID: " + mpiId));
    }

    @Transactional(readOnly = true)
    public PatientDto.Response findByNationalId(String nationalId) {
        return patientRepository.findByNationalId(nationalId)
                .map(patientMapper::toResponse)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with National ID: " + nationalId));
    }

    @Transactional(readOnly = true)
    public List<PatientDto.Response> searchPatients(PatientDto.SearchRequest searchRequest) {
        List<Patient> patients = patientRepository.searchPatients(
                searchRequest.getFirstName(),
                searchRequest.getLastName(),
                null
        );
        return patientMapper.toResponseList(patients);
    }

    public PatientDto.Response updatePatient(UUID patientId, PatientDto.UpdateRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));

        patientMapper.updateEntityFromRequest(request, patient);
        patient = patientRepository.save(patient);
        log.info("Updated patient: {}", patientId);
        return patientMapper.toResponse(patient);
    }

    public void deactivatePatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));
        patient.setActive(false);
        patientRepository.save(patient);
        log.info("Deactivated patient: {}", patientId);
    }

    private void linkPatientToFacility(UUID patientId, PatientDto.CreateRequest request) {
        boolean alreadyLinked = facilityPatientLinkRepository
                .existsByPatientIdAndFacilityId(patientId, request.getFacilityId());

        if (!alreadyLinked) {
            FacilityPatientLink link = FacilityPatientLink.builder()
                    .patientId(patientId)
                    .facilityId(request.getFacilityId())
                    .facilityName(request.getFacilityName())
                    .localPatientId(request.getLocalPatientId())
                    .build();
            facilityPatientLinkRepository.save(link);
        }
    }
}
