package com.medic.pharmacy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Drug interaction checker.
 *
 * In production this should integrate with an external clinical database
 * such as DrugBank API or OpenFDA. This implementation contains a curated
 * set of the most critical interactions as a safe fallback.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DrugInteractionService {

    private final ObjectMapper objectMapper;

    /**
     * Critical drug-drug interactions.
     * Key: drug name (lowercase), Value: set of drugs it interacts with and severity.
     */
    private static final Map<String, List<Map<String, String>>> KNOWN_INTERACTIONS = Map.of(
        "warfarin", List.of(
            Map.of("drug", "aspirin", "severity", "HIGH",
                   "description", "Increased bleeding risk - monitor INR closely"),
            Map.of("drug", "ibuprofen", "severity", "HIGH",
                   "description", "NSAIDs increase anticoagulation effect"),
            Map.of("drug", "amoxicillin", "severity", "MODERATE",
                   "description", "Some antibiotics may increase warfarin effect")
        ),
        "metformin", List.of(
            Map.of("drug", "alcohol", "severity", "HIGH",
                   "description", "Risk of lactic acidosis"),
            Map.of("drug", "contrast dye", "severity", "HIGH",
                   "description", "Hold metformin before contrast procedures")
        ),
        "simvastatin", List.of(
            Map.of("drug", "erythromycin", "severity", "HIGH",
                   "description", "Increased risk of myopathy/rhabdomyolysis"),
            Map.of("drug", "clarithromycin", "severity", "HIGH",
                   "description", "Increased risk of myopathy/rhabdomyolysis")
        ),
        "ssri", List.of(
            Map.of("drug", "tramadol", "severity", "HIGH",
                   "description", "Risk of serotonin syndrome"),
            Map.of("drug", "maoi", "severity", "CRITICAL",
                   "description", "CONTRAINDICATED: severe serotonin syndrome risk")
        )
    );

    /**
     * Checks a list of drugs for known interactions.
     *
     * @param drugsJson JSON array of drug objects with a "drugName" field
     * @return list of interaction warning maps, empty if none found
     */
    public List<Map<String, String>> checkInteractions(String drugsJson) {
        List<Map<String, String>> warnings = new ArrayList<>();

        try {
            List<Map<String, Object>> drugs = objectMapper.readValue(
                    drugsJson, new TypeReference<>() {});

            List<String> drugNames = drugs.stream()
                    .map(d -> d.getOrDefault("drugName", "").toString().toLowerCase())
                    .filter(name -> !name.isBlank())
                    .toList();

            for (int i = 0; i < drugNames.size(); i++) {
                String drug = drugNames.get(i);

                // Check against our known interactions table
                if (KNOWN_INTERACTIONS.containsKey(drug)) {
                    for (Map<String, String> interaction : KNOWN_INTERACTIONS.get(drug)) {
                        String interactingDrug = interaction.get("drug");
                        if (drugNames.contains(interactingDrug)) {
                            warnings.add(Map.of(
                                "drug1", drug,
                                "drug2", interactingDrug,
                                "severity", interaction.get("severity"),
                                "description", interaction.get("description")
                            ));
                            log.warn("Drug interaction detected: {} + {} [{}]",
                                     drug, interactingDrug, interaction.get("severity"));
                        }
                    }
                }

                // Check all pairs for partial name matches
                for (int j = i + 1; j < drugNames.size(); j++) {
                    String other = drugNames.get(j);
                    KNOWN_INTERACTIONS.forEach((key, interactions) -> {
                        if (drug.contains(key) || key.contains(drug)) {
                            interactions.stream()
                                .filter(ia -> other.contains(ia.get("drug")) || ia.get("drug").contains(other))
                                .forEach(ia -> warnings.add(Map.of(
                                    "drug1", drug,
                                    "drug2", other,
                                    "severity", ia.get("severity"),
                                    "description", ia.get("description")
                                )));
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error checking drug interactions: {}", e.getMessage(), e);
        }

        return warnings;
    }
}
