-- BUG-01 / TC-12: a specialty can be stopped (hidden from the catalog) without deleting it or its history.
ALTER TABLE cams_specialties ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
