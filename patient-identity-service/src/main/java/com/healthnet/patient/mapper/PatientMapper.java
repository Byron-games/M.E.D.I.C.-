package com.healthnet.patient.mapper;

import com.healthnet.patient.model.dto.PatientDto;
import com.healthnet.patient.model.entity.Patient;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mpiId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Patient toEntity(PatientDto.CreateRequest request);

    PatientDto.Response toResponse(Patient patient);

    List<PatientDto.Response> toResponseList(List<Patient> patients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mpiId", ignore = true)
    @Mapping(target = "nationalId", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "bloodType", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(PatientDto.UpdateRequest request, @MappingTarget Patient patient);
}
