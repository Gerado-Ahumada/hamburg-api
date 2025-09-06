-- Insertar usuarios (contraseña: admin)
INSERT INTO usuarios (username, password, nombre, apellido, email, activo, rol) VALUES
('admin', '$2a$10$yRxRYK/S.j6K5NXwJqnE3.dHB2h7gQoXjG/bckJZzI.d9HGHMjFge', 'Administrador', 'Sistema', 'admin@hamburg.com', true, 'ROLE_ADMIN');