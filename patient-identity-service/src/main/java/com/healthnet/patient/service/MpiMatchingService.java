package com.healthnet.patient.service;

import com.healthnet.patient.model.dto.PatientDto;
import com.healthnet.patient.model.entity.Patient;
import com.healthnet.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Master Patient Index (MPI) probabilistic matching service.
 *
 * Uses a weighted scoring algorithm to determine the likelihood that
 * two records refer to the same person. Critical for preventing
 * duplicate records across facilities.
 *
 * Scoring weights:
 *   - National ID (exact):    40 points
 *   - First name (exact):     15 points
 *   - Last name (exact):      15 points
 *   - Date of birth (exact):  20 points
 *   - Phone number:           10 points
 *   Total possible:          100 points
 *   Threshold for match:      90 points
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MpiMatchingService {

    private static final int NATIONAL_ID_WEIGHT  = 40;
    private static final int FIRST_NAME_WEIGHT   = 15;
    private static final int LAST_NAME_WEIGHT    = 15;
    private static final int DATE_OF_BIRTH_WEIGHT = 20;
    private static final int PHONE_WEIGHT        = 10;

    private final PatientRepository patientRepository;

    public List<Patient> findProbableMatches(String firstName, String lastName, LocalDate dateOfBirth) {
        return patientRepository.findProbableMatches(firstName, lastName, dateOfBirth);
    }

    /**
     * Calculates a confidence score (0.0 to 1.0) that two records are the same patient.
     */
    public double calculateMatchConfidence(PatientDto.CreateRequest incoming, Patient existing) {
        int score = 0;

        // National ID is the strongest identifier
        if (incoming.getNationalId() != null && incoming.getNationalId().equalsIgnoreCase(existing.getNationalId())) {
            score += NATIONAL_ID_WEIGHT;
        }

        // Name matching (case-insensitive)
        if (incoming.getFirstName() != null && incoming.getFirstName().equalsIgnoreCase(existing.getFirstName())) {
            score += FIRST_NAME_WEIGHT;
        }
        if (incoming.getLastName() != null && incoming.getLastName().equalsIgnoreCase(existing.getLastName())) {
            score += LAST_NAME_WEIGHT;
        }

        // Date of birth
        if (incoming.getDateOfBirth() != null && incoming.getDateOfBirth().equals(existing.getDateOfBirth())) {
            score += DATE_OF_BIRTH_WEIGHT;
        }

        // Phone number
        if (incoming.getPhoneNumber() != null && incoming.getPhoneNumber().equals(existing.getPhoneNumber())) {
            score += PHONE_WEIGHT;
        }

        double confidence = score / 100.0;
        log.debug("MPI match confidence for patient {}: {}%", existing.getMpiId(), score);
        return confidence;
    }

    /**
     * Calculates confidence between two existing Patient records (for deduplication jobs).
     */
    public double calculateMatchConfidence(Patient p1, Patient p2) {
        int score = 0;

        if (p1.getNationalId() != null && p1.getNationalId().equalsIgnoreCase(p2.getNationalId())) {
            score += NATIONAL_ID_WEIGHT;
        }
        if (p1.getFirstName() != null && p1.getFirstName().equalsIgnoreCase(p2.getFirstName())) {
            score += FIRST_NAME_WEIGHT;
        }
        if (p1.getLastName() != null && p1.getLastName().equalsIgnoreCase(p2.getLastName())) {
            score += LAST_NAME_WEIGHT;
        }
        if (p1.getDateOfBirth() != null && p1.getDateOfBirth().equals(p2.getDateOfBirth())) {
            score += DATE_OF_BIRTH_WEIGHT;
        }
        if (p1.getPhoneNumber() != null && p1.getPhoneNumber().equals(p2.getPhoneNumber())) {
            score += PHONE_WEIGHT;
        }

        return score / 100.0;
    }
}
