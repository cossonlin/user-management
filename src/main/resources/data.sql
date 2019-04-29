INSERT INTO roles(name) VALUES('ROLE_ADMIN');
INSERT INTO roles(name) VALUES('ROLE_MANAGER');
INSERT INTO roles(name) VALUES('ROLE_USER');

--Initial first admin user for further operation
--username/password: admin/admin
INSERT INTO users(username, email, password) VALUES('admin', 'admin@test.com', '$2a$10$1HizIEa/BK9xLFa.UfMw1eyC3E0074EkJA74V/Tz9OdOy5ECqMtHG');
INSERT INTO user_roles(user_id, role_id) VALUES(1, 1);