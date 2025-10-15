-- insert into authors(full_name)
-- values ('Author_1'), ('Author_2'), ('Author_3');
--
-- insert into genres(name)
-- values ('Genre_1'), ('Genre_2'), ('Genre_3'),
--        ('Genre_4'), ('Genre_5'), ('Genre_6');
--
-- insert into books(title, author_id)
-- values ('BookTitle_1', 1), ('BookTitle_2', 2), ('BookTitle_3', 3);
--
-- insert into books_genres(book_id, genre_id)
-- values (1, 1),   (1, 2),
--        (2, 3),   (2, 4),
--        (3, 5),   (3, 6);

INSERT INTO authors(full_name)
SELECT 'Author_' || x
FROM SYSTEM_RANGE(1, 10000);

INSERT INTO genres(name)
SELECT 'Genre_' || x
FROM SYSTEM_RANGE(1, 600);

INSERT INTO books(title, author_id)
SELECT 'BookTitle_' || x,
       ((x - 1) % 10000) + 1
FROM SYSTEM_RANGE(1, 100000);

INSERT INTO books_genres(book_id, genre_id)
SELECT b.id,
       ((b.id + o.n) % 600) + 1
FROM (SELECT x AS id FROM SYSTEM_RANGE(1, 100000)) b
CROSS JOIN (
    SELECT 0 AS n UNION ALL
    SELECT 17 UNION ALL
    SELECT 73
) o;

INSERT INTO comments(text, book_id, created_at)
SELECT 'Comment ' || b.id || '-' || c.n,
       b.id,
       TIMESTAMPADD('SECOND', (b.id * 7 + c.n) * 13, TIMESTAMP '2021-01-01 00:00:00')
FROM (SELECT x AS id FROM SYSTEM_RANGE(1, 100000)) b
JOIN (
    SELECT 1 AS n UNION ALL
    SELECT 2 UNION ALL
    SELECT 3
) c
  ON (b.id % 4) >= c.n - 1;
