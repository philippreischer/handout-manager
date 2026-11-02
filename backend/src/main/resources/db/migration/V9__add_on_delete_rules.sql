-- V9__add_on_delete_rules.sql

ALTER TABLE membership
DROP CONSTRAINT fk_membership_user_id,
    ADD CONSTRAINT fk_membership_user_id
        FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE;

ALTER TABLE membership
DROP CONSTRAINT fk_membership_project_id,
    ADD CONSTRAINT fk_membership_project_id
        FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE;

ALTER TABLE resource
DROP CONSTRAINT fk_resource_project_id,
    ADD CONSTRAINT fk_resource_project_id
        FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE;

ALTER TABLE permission
DROP CONSTRAINT fk_permission_user_id,
    ADD CONSTRAINT fk_permission_user_id
        FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE;

ALTER TABLE permission
DROP CONSTRAINT fk_permission_resource_id,
    ADD CONSTRAINT fk_permission_resource_id
        FOREIGN KEY (resource_id) REFERENCES resource (id) ON DELETE CASCADE;

