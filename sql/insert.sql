-- Thêm nhân viên
INSERT INTO employees (name, username, password_hash, role) 
VALUES ('Nguyen Van A', 'nva', 'hashed_password', 'ROLE_ISSUE');

INSERT INTO products (name, description, unit, price)
VALUES ('Sản phẩm 1', 'Mô tả SP1', 'cái', 100000),
       ('Sản phẩm 2', 'Mô tả SP2', 'cái', 200000);

INSERT INTO inventory (product_id, quantity)
VALUES (1, 10), (2, 5);
