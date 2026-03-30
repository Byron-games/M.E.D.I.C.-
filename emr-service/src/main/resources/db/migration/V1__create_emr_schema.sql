-- V1__create_emr_schema.sql
CREATE TABLE medical_records (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mpi_id                  VARCHAR(20)  NOT NULL,
    facility_id             VARCHAR(100) NOT NULL,
    facility_name           VARCHAR(200),
    attending_clinician_id  VARCHAR(100) NOT NULL,
    attending_clinician_name VARCHAR(200),
    record_type             VARCHAR(50)  NOT NULL,
    visit_date              TIMESTAMP    NOT NULL,
    chief_complaint         VARCHAR(1000),
    clinical_notes          JSONB,
    diagnosis_codes         JSONB,
    treatments              JSONB,
    lab_results             JSONB,
    vital_signs             JSONB,
    follow_up_instructions  VARCHAR(500),
    shared_to_network       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_record_mpi_id   ON medical_records (mpi_id);
CREATE INDEX idx_record_facility ON medical_records (facility_id);
CREATE INDEX idx_record_date     ON medical_records (visit_date DESC);
CREATE INDEX idx_record_type     ON medical_records (record_type);
CREATE INDEX idx_record_network  ON medical_records (shared_to_network) WHERE shared_to_network = TRUE;
