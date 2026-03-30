package com.medic.pharmacy.service;

import com.medic.pharmacy.model.dto.PrescriptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks for known dangerous drug interactions within a prescription.
 *
 * In production this integrates with a clinical drug database API
 * (e.g., OpenFDA Drug Interaction API, DrugBank, or RxNorm).
 *
 * This stub implements a curated local table of critical interactions
 * relevant to common conditions in the target region (malaria, HIV, TB, diabetes).
 * This ensures the service functions even without internet connectivity —
 * critical for rural deployments.
 */
@Component
@Slf4j
public class DrugInteractionChecker {

    /**
     * Known critical interactions: drug pair → {severity, description}
     * Keys are stored as lowercase, sorted alphabetically.
     */
    private static final Map<String, InteractionInfo> KNOWN_INTERACTIONS = Map.ofEntries(
        // HIV / Malaria interactions
        Map.entry("artemether+efavirenz",
            new InteractionInfo("MAJOR", "Efavirenz significantly reduces artemether plasma levels, risking malaria treatment failure.")),
        Map.entry("artemether+rifampicin",
            new InteractionInfo("MAJOR", "Rifampicin induces CYP3A4 and drastically reduces artemether efficacy.")),
        // TB / HIV interactions
        Map.entry("efavirenz+rifampicin",
            new InteractionInfo("MODERATE", "Rifampicin reduces efavirenz levels; dose adjustment may be required.")),
        Map.entry("isoniazid+phenytoin",
            new InteractionInfo("MAJOR", "Isoniazid inhibits phenytoin metabolism, leading to phenytoin toxicity.")),
        // Anticoagulant interactions
        Map.entry("aspirin+warfarin",
            new InteractionInfo("MAJOR", "Combination significantly increases bleeding risk.")),
        Map.entry("metronidazole+warfarin",
            new InteractionInfo("MAJOR", "Metronidazole inhibits warfarin metabolism, increasing anticoagulation and bleeding risk.")),
        // Diabetes interactions
        Map.entry("metformin+alcohol",
            new InteractionInfo("MODERATE", "Increased risk of lactic acidosis.")),
        Map.entry("ciprofloxacin+glibenclamide",
            new InteractionInfo("MODERATE", "Fluoroquinolones can cause hypoglycaemia when combined with sulfonylureas.")),
        // Cardiovascular
        Map.entry("amlodipine+simvastatin",
            new InteractionInfo("MODERATE", "Amlodipine increases simvastatin exposure, raising risk of myopathy.")),
        Map.entry("atenolol+verapamil",
            new InteractionInfo("MAJOR", "Risk of complete heart block and severe bradycardia."))
    );

    public List<PrescriptionDto.InteractionWarning> checkInteractions(
            List<PrescriptionDto.DrugItem> drugs) {

        List<PrescriptionDto.InteractionWarning> warnings = new ArrayList<>();

        for (int i = 0; i < drugs.size(); i++) {
            for (int j = i + 1; j < drugs.size(); j++) {
                String drug1 = drugs.get(i).getDrugName().toLowerCase().trim();
                String drug2 = drugs.get(j).getDrugName().toLowerCase().trim();

                // Normalise to alphabetical key
                String key = drug1.compareTo(drug2) <= 0
                        ? drug1 + "+" + drug2
                        : drug2 + "+" + drug1;

                InteractionInfo info = KNOWN_INTERACTIONS.get(key);
                if (info != null) {
                    log.warn("Drug interaction detected: {} + {} → {} — {}",
                            drug1, drug2, info.severity(), info.description());
                    warnings.add(PrescriptionDto.InteractionWarning.builder()
                            .drug1(drugs.get(i).getDrugName())
                            .drug2(drugs.get(j).getDrugName())
                            .severity(info.severity())
                            .description(info.description())
                            .build());
                }
            }
        }

        return warnings;
    }

    private record InteractionInfo(String severity, String description) {}
}
