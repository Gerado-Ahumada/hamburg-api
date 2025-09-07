-- Insertar roles
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN');
INSERT INTO roles (nombre) VALUES ('ROLE_PLAYER');

-- Insertar estados
INSERT INTO estados (nombre) VALUES ('ACTIVE');
INSERT INTO estados (nombre) VALUES ('DESACTIVE');

-- Insertar usuario administrador (contraseña: admin)
INSERT INTO usuarios (username, password, nombre, apellido, email, telefono, categoria_jugador, estado_id) VALUES
('admin', '$2a$10$yRxRYK/S.j6K5NXwJqnE3.dHB2h7gQoXjG/bckJZzI.d9HGHMjFge', 'Administrador', 'Sistema', 'admin@hamburg.com', '123456789', 'N/A', 1);

-- Asignar rol de administrador al usuario admin
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1, 1);