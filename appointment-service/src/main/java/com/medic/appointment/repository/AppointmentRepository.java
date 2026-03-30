package com.medic.appointment.repository;

import com.medic.appointment.model.entity.Appointment;
import com.medic.appointment.model.entity.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByMpiIdOrderByScheduledAtDesc(String mpiId);

    List<Appointment> findByClinicianIdAndScheduledAtBetween(
            String clinicianId, LocalDateTime from, LocalDateTime to);

    List<Appointment> findByFacilityIdAndScheduledAtBetween(
            String facilityId, LocalDateTime from, LocalDateTime to);

    /** Check for scheduling conflicts for a clinician */
    @Query("SELECT a FROM Appointment a WHERE a.clinicianId = :clinicianId " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.scheduledAt < :end AND FUNCTION('addminutes', a.scheduledAt, a.durationMinutes) > :start")
    List<Appointment> findConflictingAppointments(
            @Param("clinicianId") String clinicianId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Appointment> findByStatusAndScheduledAtBefore(AppointmentStatus status, LocalDateTime cutoff);
}
