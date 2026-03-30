package com.medic.patient.repository;

import com.medic.patient.model.entity.FacilityPatientLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FacilityPatientLinkRepository extends JpaRepository<FacilityPatientLink, UUID> {
    List<FacilityPatientLink> findByPatientId(UUID patientId);
    boolean existsByPatientIdAndFacilityId(UUID patientId, String facilityId);
}
