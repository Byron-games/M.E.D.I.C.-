-- V1__create_analytics_schema.sql
-- IMPORTANT: This table stores ONLY aggregate, anonymized public health data.
-- NO patient identifiers (name, MPI ID, national ID) are stored here.
CREATE TABLE disease_snapshots (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_date             DATE        NOT NULL,
    icd_code                  VARCHAR(20) NOT NULL,
    icd_description           VARCHAR(200),
    region                    VARCHAR(100) NOT NULL,
    case_count                BIGINT      NOT NULL DEFAULT 0,
    new_cases_vs_previous_week BIGINT,
    outbreak_alert            BOOLEAN     NOT NULL DEFAULT FALSE,
    alert_level               VARCHAR(10) NOT NULL DEFAULT 'GREEN',
    created_at                TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (snapshot_date, icd_code, region)
);

CREATE INDEX idx_snap_date   ON disease_snapshots (snapshot_date DESC);
CREATE INDEX idx_snap_icd    ON disease_snapshots (icd_code);
CREATE INDEX idx_snap_region ON disease_snapshots (region);
CREATE INDEX idx_snap_alert  ON disease_snapshots (outbreak_alert) WHERE outbreak_alert = TRUE;
