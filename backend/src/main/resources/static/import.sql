CREATE TRIGGER before_invoice_insert
    BEFORE INSERT ON invoices
    FOR EACH ROW
    SET NEW.converted_amount_pre_vat = NEW.original_amount * NEW.exchange_rate,
        NEW.total_amount_with_vat = (NEW.original_amount * NEW.exchange_rate) + NEW.vat;

CREATE TRIGGER before_invoice_update
    BEFORE UPDATE ON invoices
    FOR EACH ROW
    SET NEW.converted_amount_pre_vat = NEW.original_amount * NEW.exchange_rate,
        NEW.total_amount_with_vat = (NEW.original_amount * NEW.exchange_rate) + NEW.vat;

INSERT INTO roles (role_id, role_name) VALUES
    (1, 'ADMIN'),
    (2, 'STAFF');

ALTER TABLE roles AUTO_INCREMENT = 3;

INSERT INTO permissions (permission_id, permission_name) VALUES
    (1, 'READ'),
    (2, 'WRITE'),
    (3, 'DELETE'),
    (4, 'EXPORT');

ALTER TABLE permissions AUTO_INCREMENT = 5;

INSERT INTO role_permissions (role_id, permission_id) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (2, 1),
    (2, 2),
    (2, 4);