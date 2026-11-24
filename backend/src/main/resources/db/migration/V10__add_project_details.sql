--V10__add_project_details.sql

ALTER TABLE project ADD COLUMN game_system VARCHAR(255);

ALTER TABLE project ADD COLUMN description TEXT;