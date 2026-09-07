-- Passwords were stored in plaintext; the seeded admin is the only account that predates bcrypt.
UPDATE users SET password = '$2y$12$cY/.X8Kj0My8Ts7R7U6YkO.KCiL29l56BR/6lSmiN7FJgYx3BVLWe' WHERE login = 'admin';
