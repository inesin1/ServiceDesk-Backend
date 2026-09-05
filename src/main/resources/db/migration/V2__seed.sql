-- Role and status ids are referenced as constants in the routing code.
INSERT INTO roles (id, name) VALUES (1, 'Сотрудник'), (2, 'ИТ-специалист'), (3, 'Администратор');
INSERT INTO statuses (id, name) VALUES (1, 'Новая'), (2, 'Закрыта'), (3, 'В работе');

INSERT INTO ticket_sources (id, name) VALUES (1, 'Веб-интерфейс'), (2, 'Телефон'), (3, 'Личное обращение');

INSERT INTO ticket_categories (id, name) VALUES
    (1, 'Не работает компьютер'),
    (2, 'Не работает принтер'),
    (3, 'Проблема с сетью'),
    (4, 'Установка программы'),
    (5, 'Другое');

INSERT INTO departments (id, name) VALUES
    (1, 'Регистратура'),
    (2, 'Терапевтическое отделение'),
    (3, 'Хирургическое отделение'),
    (4, 'Лаборатория'),
    (5, 'Администрация');

INSERT INTO users (id, name, login, password, role_id) VALUES (1, 'Администратор', 'admin', 'admin', 3);
INSERT INTO user_departments (user_id, department_id) VALUES (1, 5);

-- Explicit ids above leave the sequences at 1, so the next insert would collide.
SELECT setval('roles_id_seq', (SELECT max(id) FROM roles));
SELECT setval('statuses_id_seq', (SELECT max(id) FROM statuses));
SELECT setval('ticket_sources_id_seq', (SELECT max(id) FROM ticket_sources));
SELECT setval('ticket_categories_id_seq', (SELECT max(id) FROM ticket_categories));
SELECT setval('departments_id_seq', (SELECT max(id) FROM departments));
SELECT setval('users_id_seq', (SELECT max(id) FROM users));
