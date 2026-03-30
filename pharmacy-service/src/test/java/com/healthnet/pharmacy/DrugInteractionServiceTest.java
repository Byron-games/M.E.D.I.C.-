package com.healthnet.pharmacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthnet.pharmacy.service.DrugInteractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Drug Interaction Service Unit Tests")
class DrugInteractionServiceTest {

    private DrugInteractionService service;

    @BeforeEach
    void setUp() {
        service = new DrugInteractionService(new ObjectMapper());
    }

    @Test
    @DisplayName("Should detect warfarin + aspirin HIGH severity interaction")
    void shouldDetectWarfarinAspirinInteraction() throws Exception {
        String drugs = """
            [
              {"drugName": "warfarin", "dosage": "5mg", "frequency": "daily"},
              {"drugName": "aspirin",  "dosage": "100mg", "frequency": "daily"}
            ]
            """;

        List<Map<String, String>> warnings = service.checkInteractions(drugs);

        assertThat(warnings).isNotEmpty();
        assertThat(warnings).anyMatch(w ->
            w.get("severity").equals("HIGH") &&
            w.get("description").contains("bleeding"));
    }

    @Test
    @DisplayName("Should detect MAOI + SSRI CRITICAL interaction")
    void shouldDetectMaoiSsriCriticalInteraction() throws Exception {
        String drugs = """
            [
              {"drugName": "ssri",  "dosage": "20mg", "frequency": "daily"},
              {"drugName": "maoi",  "dosage": "10mg", "frequency": "daily"}
            ]
            """;

        List<Map<String, String>> warnings = service.checkInteractions(drugs);

        assertThat(warnings).anyMatch(w -> w.get("severity").equals("CRITICAL"));
    }

    @Test
    @DisplayName("Should return empty list for safe drug combinations")
    void shouldReturnEmptyForSafeCombination() throws Exception {
        String drugs = """
            [
              {"drugName": "paracetamol", "dosage": "500mg", "frequency": "every 6 hours"},
              {"drugName": "amoxicillin", "dosage": "500mg", "frequency": "every 8 hours"}
            ]
            """;

        List<Map<String, String>> warnings = service.checkInteractions(drugs);

        assertThat(warnings).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for single drug")
    void shouldReturnEmptyForSingleDrug() throws Exception {
        String drugs = """
            [{"drugName": "metformin", "dosage": "500mg", "frequency": "twice daily"}]
            """;

        List<Map<String, String>> warnings = service.checkInteractions(drugs);

        assertThat(warnings).isEmpty();
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully without throwing")
    void shouldHandleInvalidJsonGracefully() {
        List<Map<String, String>> warnings = service.checkInteractions("not-valid-json");

        assertThat(warnings).isEmpty(); // Should not throw, just return empty
    }
}
