package com.healthnet.emr.repository;

import com.healthnet.emr.model.entity.MedicalRecord;
import com.healthnet.emr.model.entity.MedicalRecord.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    Page<MedicalRecord> findByMpiIdOrderByVisitDateDesc(String mpiId, Pageable pageable);

    List<MedicalRecord> findByMpiIdAndRecordType(String mpiId, RecordType recordType);

    @Query("SELECT r FROM MedicalRecord r WHERE r.mpiId = :mpiId " +
           "AND r.visitDate BETWEEN :from AND :to ORDER BY r.visitDate DESC")
    List<MedicalRecord> findByMpiIdAndDateRange(
            @Param("mpiId") String mpiId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    List<MedicalRecord> findByFacilityIdAndVisitDateAfter(String facilityId, LocalDateTime after);

    /** For analytics: count records by region and type in a date range */
    @Query("SELECT r.recordType, COUNT(r) FROM MedicalRecord r " +
           "WHERE r.visitDate BETWEEN :from AND :to AND r.sharedToNetwork = true " +
           "GROUP BY r.recordType")
    List<Object[]> countByRecordTypeForPeriod(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
