-- Insertar roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_PLAYER');

-- Insertar estados
INSERT INTO statuses (name) VALUES ('ACTIVE');
INSERT INTO statuses (name) VALUES ('INACTIVE');

-- Insertar usuario administrador (contraseña: admin)
INSERT INTO users (username, password, name, last_name, email, phone, player_category, status_id) VALUES
('admin', '$2a$10$yRxRYK/S.j6K5NXwJqnE3.dHB2h7gQoXjG/bckJZzI.d9HGHMjFge', 'Administrator', 'System', 'admin@hamburg.com', '123456789', 'N/A', 1);

-- Asignar rol de administrador al usuario admin
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);