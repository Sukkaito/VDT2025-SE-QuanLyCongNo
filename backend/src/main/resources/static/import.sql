INSERT INTO roles (role_id, role_name) VALUES
    (1, 'ADMIN'),
    (2, 'STAFF');

SELECT setval('roles_role_id_seq', 3, false);

INSERT INTO permissions (permission_id, permission_name) VALUES
    (1, 'READ'),
    (2, 'WRITE'),
    (3, 'DELETE'),
    (4, 'EXPORT');

SELECT setval('permissions_permission_id_seq', 5, false);

INSERT INTO role_permissions (role_id, permission_id) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (2, 1),
    (2, 2),
    (2, 4);

