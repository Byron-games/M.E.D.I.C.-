-- V1__create_telemedicine_schema.sql
CREATE TABLE telemedicine_sessions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id       UUID         NOT NULL UNIQUE,
    mpi_id               VARCHAR(20)  NOT NULL,
    clinician_id         VARCHAR(100) NOT NULL,
    provider_session_id  VARCHAR(200) NOT NULL UNIQUE,
    patient_join_url     VARCHAR(500) NOT NULL,
    clinician_join_url   VARCHAR(500) NOT NULL,
    status               VARCHAR(30)  NOT NULL DEFAULT 'CREATED',
    scheduled_at         TIMESTAMP,
    started_at           TIMESTAMP,
    ended_at             TIMESTAMP,
    duration_seconds     BIGINT,
    low_bandwidth_mode   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tele_mpi       ON telemedicine_sessions (mpi_id);
CREATE INDEX idx_tele_clinician ON telemedicine_sessions (clinician_id);
CREATE INDEX idx_tele_status    ON telemedicine_sessions (status);
