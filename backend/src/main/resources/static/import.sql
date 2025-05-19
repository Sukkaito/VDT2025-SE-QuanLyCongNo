
INSERT INTO roles (role_name) VALUES
    ('ADMIN'),
    ('EMPLOYEE');

INSERT INTO permissions (permission_name) VALUES
    ('READ'),
    ('WRITE'),
    ('DELETE'),
    ('EXPORT');

INSERT INTO role_permissions (role_id, permission_id) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (2, 1),
    (2, 2),
    (2, 4);