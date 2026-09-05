CREATE TABLE roles (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE departments (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE statuses (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE ticket_categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE ticket_sources (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    login      VARCHAR(64)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role_id    INT          NOT NULL REFERENCES roles (id),
    phone      VARCHAR(32),
    tg_chat_id BIGINT
);

CREATE TABLE user_departments (
    user_id       INT NOT NULL REFERENCES users (id),
    department_id INT NOT NULL REFERENCES departments (id),
    PRIMARY KEY (user_id, department_id)
);

CREATE TABLE tickets (
    id          SERIAL PRIMARY KEY,
    creator_id  INT       NOT NULL REFERENCES users (id),
    executor_id INT       REFERENCES users (id),
    details     TEXT,
    source_id   INT       NOT NULL REFERENCES ticket_sources (id),
    category_id INT       NOT NULL REFERENCES ticket_categories (id),
    status_id   INT       NOT NULL REFERENCES statuses (id),
    created_at  TIMESTAMP NOT NULL,
    closed_at   TIMESTAMP,
    time_limit  TIMESTAMP NOT NULL
);

CREATE INDEX tickets_creator_id_idx ON tickets (creator_id);
CREATE INDEX tickets_status_id_idx ON tickets (status_id);

CREATE TABLE ticket_comments (
    id         SERIAL PRIMARY KEY,
    ticket_id  INT       NOT NULL REFERENCES tickets (id),
    creator_id INT       NOT NULL REFERENCES users (id),
    text       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX ticket_comments_ticket_id_idx ON ticket_comments (ticket_id);
