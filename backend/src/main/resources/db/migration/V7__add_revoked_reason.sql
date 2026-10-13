ALTER TABLE refresh_token ADD COLUMN revoked_reason VARCHAR(30);

UPDATE refresh_token SET revoked_reason = 'ROTATED' WHERE revoked_at IS NOT NULL;

ALTER TABLE refresh_token ADD CONSTRAINT ck_refresh_token_revoked
    CHECK ((revoked_at IS NULL) = (revoked_reason IS NULL));