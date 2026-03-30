-- V1__create_patients_schema.sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE patients (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mpi_id          VARCHAR(20)  NOT NULL UNIQUE,
    national_id     VARCHAR(50)  NOT NULL UNIQUE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    date_of_birth   DATE         NOT NULL,
    gender          VARCHAR(10)  NOT NULL,
    phone_number    VARCHAR(20),
    email           VARCHAR(150),
    address         TEXT,
    region          VARCHAR(100),
    blood_type      VARCHAR(5),
    known_allergies TEXT,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_patient_national_id  ON patients (national_id);
CREATE INDEX idx_patient_name_dob     ON patients (first_name, last_name, date_of_birth);
CREATE INDEX idx_patient_region       ON patients (region) WHERE active = TRUE;
CREATE INDEX idx_patient_mpi_id       ON patients (mpi_id);

CREATE TABLE facility_patient_links (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id       UUID         NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    facility_id      VARCHAR(100) NOT NULL,
    facility_name    VARCHAR(200),
    facility_type    VARCHAR(50),
    local_patient_id VARCHAR(100),
    linked_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (patient_id, facility_id)
);

CREATE INDEX idx_facility_link_patient ON facility_patient_links (patient_id);
CREATE INDEX idx_facility_link_facility ON facility_patient_links (facility_id);

-- Audit log table
CREATE TABLE patient_audit_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id  UUID         NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    facility_id VARCHAR(100),
    details     JSONB,
    performed_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_patient ON patient_audit_log (patient_id);
CREATE INDEX idx_audit_time    ON patient_audit_log (performed_at);
