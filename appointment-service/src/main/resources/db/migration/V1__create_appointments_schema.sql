-- V1__create_appointments_schema.sql
CREATE TABLE appointments (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mpi_id                VARCHAR(20)  NOT NULL,
    patient_name          VARCHAR(200) NOT NULL,
    facility_id           VARCHAR(100) NOT NULL,
    facility_name         VARCHAR(200),
    clinician_id          VARCHAR(100) NOT NULL,
    clinician_name        VARCHAR(200),
    clinician_specialty   VARCHAR(100),
    type                  VARCHAR(30)  NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    scheduled_at          TIMESTAMP    NOT NULL,
    duration_minutes      INTEGER      DEFAULT 30,
    notes                 VARCHAR(500),
    telemedicine_join_url VARCHAR(500),
    cancellation_reason   VARCHAR(300),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_appt_mpi_id    ON appointments (mpi_id);
CREATE INDEX idx_appt_clinician ON appointments (clinician_id);
CREATE INDEX idx_appt_facility  ON appointments (facility_id);
CREATE INDEX idx_appt_scheduled ON appointments (scheduled_at);
CREATE INDEX idx_appt_status    ON appointments (status);
