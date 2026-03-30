package com.medic.analytics.repository;

import com.medic.analytics.model.entity.DiseaseSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DiseaseSnapshotRepository extends JpaRepository<DiseaseSnapshot, UUID> {
    List<DiseaseSnapshot> findByOutbreakAlertTrueAndSnapshotDateAfter(LocalDate after);
    List<DiseaseSnapshot> findByRegionAndSnapshotDateBetween(String region, LocalDate from, LocalDate to);
    List<DiseaseSnapshot> findByIcdCodeAndSnapshotDateBetween(String icdCode, LocalDate from, LocalDate to);
}
