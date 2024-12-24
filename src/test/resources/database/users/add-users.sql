INSERT INTO users (id, email, first_name, last_name, password)
VALUES (3, 'eto@gmail.com', 'Samuel', 'Eto', '12345678910'),
       (4, 'messi@gmail.com', 'Lionel', 'Messi', '1112131415');

INSERT INTO users_roles (user_id, role_id)
VALUES (3, 1),
       (4, 1);