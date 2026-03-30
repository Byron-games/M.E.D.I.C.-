package com.healthnet.pharmacy.service;

import com.healthnet.pharmacy.model.dto.PrescriptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Drug Interaction Checker Tests")
class DrugInteractionCheckerTest {

    private DrugInteractionChecker checker;

    @BeforeEach
    void setUp() {
        checker = new DrugInteractionChecker();
    }

    private PrescriptionDto.DrugItem drug(String name) {
        return PrescriptionDto.DrugItem.builder()
                .drugName(name).dosage("1 tablet").frequency("daily").duration("7 days").build();
    }

    @Test
    @DisplayName("Should detect MAJOR interaction between Artemether and Efavirenz")
    void shouldDetectArtemetherEfavirenzInteraction() {
        List<PrescriptionDto.DrugItem> drugs = List.of(drug("Artemether"), drug("Efavirenz"));
        List<PrescriptionDto.InteractionWarning> warnings = checker.checkInteractions(drugs);

        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0).getSeverity()).isEqualTo("MAJOR");
        assertThat(warnings.get(0).getDrug1()).isIn("Artemether", "Efavirenz");
        assertThat(warnings.get(0).getDrug2()).isIn("Artemether", "Efavirenz");
    }

    @Test
    @DisplayName("Should detect MAJOR interaction between Aspirin and Warfarin")
    void shouldDetectAspirinWarfarinInteraction() {
        List<PrescriptionDto.DrugItem> drugs = List.of(drug("Aspirin"), drug("Warfarin"));
        List<PrescriptionDto.InteractionWarning> warnings = checker.checkInteractions(drugs);

        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0).getSeverity()).isEqualTo("MAJOR");
    }

    @Test
    @DisplayName("Should detect multiple interactions in one prescription")
    void shouldDetectMultipleInteractions() {
        List<PrescriptionDto.DrugItem> drugs = List.of(
                drug("Aspirin"), drug("Warfarin"), drug("Metronidazole"));
        List<PrescriptionDto.InteractionWarning> warnings = checker.checkInteractions(drugs);

        // Aspirin+Warfarin AND Metronidazole+Warfarin
        assertThat(warnings).hasSize(2);
    }

    @Test
    @DisplayName("Should return no warnings for safe drug combinations")
    void shouldReturnNoWarningsForSafeDrugs() {
        List<PrescriptionDto.DrugItem> drugs = List.of(
                drug("Paracetamol"), drug("Amoxicillin"), drug("ORS"));
        List<PrescriptionDto.InteractionWarning> warnings = checker.checkInteractions(drugs);

        assertThat(warnings).isEmpty();
    }

    @Test
    @DisplayName("Should be case-insensitive in drug name matching")
    void shouldBeCaseInsensitive() {
        List<PrescriptionDto.DrugItem> drugs = List.of(drug("ASPIRIN"), drug("warfarin"));
        List<PrescriptionDto.InteractionWarning> warnings = checker.checkInteractions(drugs);

        assertThat(warnings).hasSize(1);
    }

    @Test
    @DisplayName("Single drug should return no interaction warnings")
    void singleDrugShouldHaveNoInteractions() {
        List<PrescriptionDto.DrugItem> drugs = List.of(drug("Metformin"));
        List<PrescriptionDto.InteractionWarning> warnings = checker.checkInteractions(drugs);

        assertThat(warnings).isEmpty();
    }
}
