-- V1__create_pharmacy_schema.sql
CREATE TABLE prescriptions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rx_code              VARCHAR(20)  NOT NULL UNIQUE,
    mpi_id               VARCHAR(20)  NOT NULL,
    prescriber_id        VARCHAR(100) NOT NULL,
    prescriber_name      VARCHAR(200),
    prescriber_license_no VARCHAR(100),
    issuing_facility_id  VARCHAR(100) NOT NULL,
    pharmacy_id          VARCHAR(100),
    pharmacy_name        VARCHAR(200),
    drugs                JSONB        NOT NULL,
    interaction_warnings JSONB,
    status               VARCHAR(30)  NOT NULL DEFAULT 'ISSUED',
    expiry_date          DATE         NOT NULL,
    notes                VARCHAR(500),
    dispensed_at         TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rx_mpi_id   ON prescriptions (mpi_id);
CREATE INDEX idx_rx_code     ON prescriptions (rx_code);
CREATE INDEX idx_rx_status   ON prescriptions (status);
CREATE INDEX idx_rx_pharmacy ON prescriptions (pharmacy_id);
CREATE INDEX idx_rx_expiry   ON prescriptions (expiry_date) WHERE status = 'ISSUED';
