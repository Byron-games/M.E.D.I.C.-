package com.medic.patient.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.patient.model.dto.PatientDto;
import com.medic.patient.model.entity.Patient.Gender;
import com.medic.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Patient Identity Service Integration Tests")
class PatientIdentityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("medic_patients_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
    }

    private PatientDto.CreateRequest buildValidCreateRequest(String nationalId) {
        return PatientDto.CreateRequest.builder()
                .nationalId(nationalId)
                .firstName("Amara")
                .lastName("Diallo")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("+237612345678")
                .email("amara.diallo@example.com")
                .region("Centre")
                .facilityId("FAC-001")
                .facilityName("Yaoundé Central Hospital")
                .localPatientId("LOC-123")
                .build();
    }

    @Test
    @DisplayName("Should register new patient and return 201 with MPI ID")
    void shouldRegisterNewPatient() throws Exception {
        var request = buildValidCreateRequest("CMR-1990-001");

        mockMvc.perform(post("/api/patients")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mpiId", startsWith("MPI-")))
                .andExpect(jsonPath("$.firstName", is("Amara")))
                .andExpect(jsonPath("$.lastName", is("Diallo")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("Should return same patient when registering same national ID from different facility")
    void shouldLinkExistingPatientOnDuplicateNationalId() throws Exception {
        var firstRequest = buildValidCreateRequest("CMR-1990-002");

        String firstResponse = mockMvc.perform(post("/api/patients")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String firstMpiId = objectMapper.readTree(firstResponse).get("mpiId").asText();

        // Second registration from a different facility with same national ID
        var secondRequest = buildValidCreateRequest("CMR-1990-002");
        secondRequest.setFacilityId("FAC-002");
        secondRequest.setFacilityName("Douala General Hospital");

        mockMvc.perform(post("/api/patients")
                .header("X-User-Id", "user-2")
                .header("X-User-Role", "CLINICIAN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mpiId", is(firstMpiId))); // Must be the SAME patient
    }

    @Test
    @DisplayName("Should find patient by MPI ID")
    void shouldFindPatientByMpiId() throws Exception {
        var createRequest = buildValidCreateRequest("CMR-1990-003");

        String createResponse = mockMvc.perform(post("/api/patients")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String mpiId = objectMapper.readTree(createResponse).get("mpiId").asText();

        mockMvc.perform(get("/api/patients/mpi/" + mpiId)
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mpiId", is(mpiId)))
                .andExpect(jsonPath("$.nationalId", is("CMR-1990-003")));
    }

    @Test
    @DisplayName("Should return 409 Conflict when high-confidence duplicate is detected")
    void shouldRejectHighConfidenceDuplicate() throws Exception {
        var firstRequest = buildValidCreateRequest("CMR-1990-004");
        mockMvc.perform(post("/api/patients")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Attempt same patient with different national ID but same name + DOB + phone
        var duplicateRequest = PatientDto.CreateRequest.builder()
                .nationalId("CMR-1990-999")  // different national ID
                .firstName("Amara")
                .lastName("Diallo")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .phoneNumber("+237612345678")  // same phone
                .facilityId("FAC-003")
                .build();

        mockMvc.perform(post("/api/patients")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Duplicate Patient Detected")));
    }

    @Test
    @DisplayName("Should return 404 for unknown MPI ID")
    void shouldReturn404ForUnknownMpiId() throws Exception {
        mockMvc.perform(get("/api/patients/mpi/MPI-DOESNOTEXIST")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Patient Not Found")));
    }

    @Test
    @DisplayName("Should return 400 when required fields are missing")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        var invalidRequest = PatientDto.CreateRequest.builder()
                .firstName("OnlyFirstName")
                .build();

        mockMvc.perform(post("/api/patients")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "CLINICIAN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", notNullValue()));
    }

    @Test
    @DisplayName("Should return 401 when no auth headers are present")
    void shouldReturn401WhenNoAuthHeaders() throws Exception {
        mockMvc.perform(get("/api/patients/mpi/MPI-ANYTHING"))
                .andExpect(status().isUnauthorized());
    }
}
