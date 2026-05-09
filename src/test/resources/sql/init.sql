CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

INSERT INTO app_user (username, password, email)
VALUES
    ('krzysiek', 'haslo123', 'krzysiek@gmail.com'),
    ('kamil', 'mocnehaslo123', 'kamil@gmail.com');