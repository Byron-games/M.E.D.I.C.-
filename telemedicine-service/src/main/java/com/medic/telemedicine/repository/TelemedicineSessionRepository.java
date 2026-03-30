package com.medic.telemedicine.repository;

import com.medic.telemedicine.model.entity.TelemedicineSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelemedicineSessionRepository extends JpaRepository<TelemedicineSession, UUID> {
    Optional<TelemedicineSession> findByAppointmentId(UUID appointmentId);
    List<TelemedicineSession> findByMpiId(String mpiId);
    List<TelemedicineSession> findByClinicianId(String clinicianId);
}
