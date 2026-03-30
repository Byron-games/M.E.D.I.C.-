package com.medic.patient.service;

import com.medic.patient.model.dto.PatientDto;
import com.medic.patient.model.entity.Patient;
import com.medic.patient.model.entity.Patient.Gender;
import com.medic.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MPI Matching Service Unit Tests")
class MpiMatchingServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private MpiMatchingService mpiMatchingService;

    private Patient existingPatient;
    private PatientDto.CreateRequest incomingRequest;

    @BeforeEach
    void setUp() {
        existingPatient = Patient.builder()
                .nationalId("CMR-001")
                .firstName("Amara")
                .lastName("Diallo")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("+237612345678")
                .build();

        incomingRequest = PatientDto.CreateRequest.builder()
                .nationalId("CMR-001")
                .firstName("Amara")
                .lastName("Diallo")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phoneNumber("+237612345678")
                .facilityId("FAC-001")
                .build();
    }

    @Test
    @DisplayName("Full match on all fields should return 100% confidence")
    void fullMatchShouldReturnMaxConfidence() {
        double confidence = mpiMatchingService.calculateMatchConfidence(incomingRequest, existingPatient);
        assertThat(confidence).isEqualTo(1.0);
    }

    @Test
    @DisplayName("National ID + name match should return 70% confidence")
    void nationalIdAndNameMatchShouldReturn70Percent() {
        incomingRequest.setDateOfBirth(LocalDate.of(1991, 1, 1)); // different DOB
        incomingRequest.setPhoneNumber(null); // no phone

        double confidence = mpiMatchingService.calculateMatchConfidence(incomingRequest, existingPatient);
        // 40 (national ID) + 15 (first name) + 15 (last name) = 70
        assertThat(confidence).isEqualTo(0.70);
    }

    @Test
    @DisplayName("Name + DOB match should return 50% confidence")
    void nameAndDobMatchShouldReturn50Percent() {
        incomingRequest.setNationalId("DIFFERENT-ID");
        incomingRequest.setPhoneNumber(null);

        double confidence = mpiMatchingService.calculateMatchConfidence(incomingRequest, existingPatient);
        // 15 (first name) + 15 (last name) + 20 (DOB) = 50
        assertThat(confidence).isEqualTo(0.50);
    }

    @Test
    @DisplayName("Only national ID match should return 40% confidence")
    void onlyNationalIdMatchShouldReturn40Percent() {
        incomingRequest.setFirstName("Different");
        incomingRequest.setLastName("Name");
        incomingRequest.setDateOfBirth(LocalDate.of(1999, 1, 1));
        incomingRequest.setPhoneNumber(null);

        double confidence = mpiMatchingService.calculateMatchConfidence(incomingRequest, existingPatient);
        assertThat(confidence).isEqualTo(0.40);
    }

    @Test
    @DisplayName("No match should return 0% confidence")
    void noMatchShouldReturnZeroConfidence() {
        incomingRequest.setNationalId("DIFFERENT");
        incomingRequest.setFirstName("John");
        incomingRequest.setLastName("Smith");
        incomingRequest.setDateOfBirth(LocalDate.of(1980, 1, 1));
        incomingRequest.setPhoneNumber("+1234567890");

        double confidence = mpiMatchingService.calculateMatchConfidence(incomingRequest, existingPatient);
        assertThat(confidence).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should call repository to find probable matches")
    void shouldCallRepositoryToFindProbableMatches() {
        when(patientRepository.findProbableMatches("Amara", "Diallo", LocalDate.of(1990, 5, 15)))
                .thenReturn(List.of(existingPatient));

        List<Patient> result = mpiMatchingService.findProbableMatches("Amara", "Diallo", LocalDate.of(1990, 5, 15));

        assertThat(result).hasSize(1);
        verify(patientRepository).findProbableMatches("Amara", "Diallo", LocalDate.of(1990, 5, 15));
    }

    @Test
    @DisplayName("Confidence above 90% threshold should trigger duplicate detection")
    void confidenceAbove90ShouldTriggerDuplicateDetection() {
        // National ID + all names + DOB = 90%
        incomingRequest.setPhoneNumber(null);

        double confidence = mpiMatchingService.calculateMatchConfidence(incomingRequest, existingPatient);
        // 40 + 15 + 15 + 20 = 90
        assertThat(confidence).isGreaterThanOrEqualTo(0.90);
    }
}
