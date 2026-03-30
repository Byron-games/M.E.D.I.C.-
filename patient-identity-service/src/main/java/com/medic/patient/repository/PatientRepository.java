package com.medic.patient.repository;

import com.medic.patient.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByMpiId(String mpiId);

    Optional<Patient> findByNationalId(String nationalId);

    boolean existsByNationalId(String nationalId);

    /**
     * Fuzzy match for MPI record linkage.
     * Searches by name + date of birth combination used when national ID is unavailable.
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) = LOWER(:firstName) AND " +
           "LOWER(p.lastName) = LOWER(:lastName) AND " +
           "p.dateOfBirth = :dateOfBirth AND " +
           "p.active = true")
    List<Patient> findByNameAndDateOfBirth(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dateOfBirth") LocalDate dateOfBirth);

    /**
     * Probabilistic match — used for deduplication when creating new records.
     * Returns patients matching at least two of three key identifiers.
     */
    @Query("SELECT p FROM Patient p WHERE p.active = true AND (" +
           "  (LOWER(p.firstName) = LOWER(:firstName) AND LOWER(p.lastName) = LOWER(:lastName)) OR " +
           "  (LOWER(p.firstName) = LOWER(:firstName) AND p.dateOfBirth = :dateOfBirth) OR " +
           "  (LOWER(p.lastName) = LOWER(:lastName) AND p.dateOfBirth = :dateOfBirth)" +
           ")")
    List<Patient> findProbableMatches(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("dateOfBirth") LocalDate dateOfBirth);

    @Query("SELECT p FROM Patient p WHERE " +
           "(:firstName IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:region IS NULL OR p.region = :region) AND " +
           "p.active = true")
    List<Patient> searchPatients(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("region") String region);

    List<Patient> findByRegionAndActive(String region, boolean active);
}
