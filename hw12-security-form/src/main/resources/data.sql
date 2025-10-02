insert into authors(full_name)
values ('Author_1'), ('Author_2'), ('Author_3');

insert into genres(name)
values ('Genre_1'), ('Genre_2'), ('Genre_3'),
       ('Genre_4'), ('Genre_5'), ('Genre_6');

insert into books(title, author_id)
values ('BookTitle_1', 1), ('BookTitle_2', 2), ('BookTitle_3', 3);

insert into books_genres(book_id, genre_id)
values (1, 1),   (1, 2),
       (2, 3),   (2, 4),
       (3, 5),   (3, 6);

insert into comments(text, book_id, created_at)
values
  ('A classic!', 1, CURRENT_TIMESTAMP()),
  ('Solid read', 1, CURRENT_TIMESTAMP()),
  ('Not my cup of tea', 2, CURRENT_TIMESTAMP());

insert into users(username, password, enabled, full_name)
values ('user', '{noop}user', true, 'User One'),
       ('admin', '{noop}admin', true, 'Administrator');

insert into user_roles(user_id, role)
values
  ((select id from users where username='user'), 'ROLE_USER'),
  ((select id from users where username='admin'), 'ROLE_ADMIN');
