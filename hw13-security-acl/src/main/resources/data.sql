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



insert into acl_sid(principal, sid) values
 (true,  'admin'),
 (true,  'user'),
 (false, 'ROLE_ADMIN'),
 (false, 'ROLE_USER');

insert into acl_class(class) values ('ru.otus.hw.models.Book');

insert into acl_object_identity(object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting)
select c.id, b.id, null,
       (select s.id from acl_sid s where s.principal = true and s.sid = 'admin'),
       false
from books b cross join acl_class c
where c.class = 'ru.otus.hw.models.Book';


-- admin: sudo - все права
insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 100, s_admin.id, 1,  true, false, false
from acl_object_identity aoi
join acl_sid s_admin on s_admin.sid = 'admin' and s_admin.principal = true;

insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 101, s_admin.id, 2,  true, false, false
from acl_object_identity aoi
join acl_sid s_admin on s_admin.sid = 'admin' and s_admin.principal = true;
п
insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 102, s_admin.id, 4,  true, false, false
from acl_object_identity aoi
join acl_sid s_admin on s_admin.sid = 'admin' and s_admin.principal = true;

insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 103, s_admin.id, 8,  true, false, false
from acl_object_identity aoi
join acl_sid s_admin on s_admin.sid = 'admin' and s_admin.principal = true;

insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 104, s_admin.id, 16, true, false, false
from acl_object_identity aoi
join acl_sid s_admin on s_admin.sid = 'admin' and s_admin.principal = true;

-- ROLE_ADMIN: - все права
insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 110, s_role_admin.id, 1,  true, false, false
from acl_object_identity aoi
join acl_sid s_role_admin on s_role_admin.sid = 'ROLE_ADMIN' and s_role_admin.principal = false;

insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 111, s_role_admin.id, 2,  true, false, false
from acl_object_identity aoi
join acl_sid s_role_admin on s_role_admin.sid = 'ROLE_ADMIN' and s_role_admin.principal = false;

insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 112, s_role_admin.id, 4,  true, false, false
from acl_object_identity aoi
join acl_sid s_role_admin on s_role_admin.sid = 'ROLE_ADMIN' and s_role_admin.principal = false;

insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 113, s_role_admin.id, 8,  true, false, false
from acl_object_identity aoi
join acl_sid s_role_admin on s_role_admin.sid = 'ROLE_ADMIN' and s_role_admin.principal = false;

insert into acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 114, s_role_admin.id, 16, true, false, false
from acl_object_identity aoi
join acl_sid s_role_admin on s_role_admin.sid = 'ROLE_ADMIN' and s_role_admin.principal = false;

-- ROLE_USER: READ только на Book id=1
insert into acl_entry(acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)
select aoi.id, 5, s_role_user.id, 1, true, false, false
from acl_object_identity aoi
join acl_sid s_role_user on s_role_user.sid='ROLE_USER' and s_role_user.principal=false
where aoi.object_id_identity = 1;