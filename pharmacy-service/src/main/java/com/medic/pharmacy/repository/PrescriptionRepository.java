package com.medic.pharmacy.repository;

import com.medic.pharmacy.model.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Optional<Prescription> findByRxCode(String rxCode);
    List<Prescription> findByMpiIdOrderByCreatedAtDesc(String mpiId);
    List<Prescription> findByPharmacyIdAndStatus(String pharmacyId, Prescription.PrescriptionStatus status);
    List<Prescription> findByStatusAndExpiryDateBefore(Prescription.PrescriptionStatus status, LocalDate date);
}
