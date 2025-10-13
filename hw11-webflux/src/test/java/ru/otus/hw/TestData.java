package ru.otus.hw;

import org.springframework.data.r2dbc.core.R2dbcEntityOperations;

public record TestData(R2dbcEntityOperations ops) {

    public void cleanAll() {
        exec("DELETE FROM comments");
        exec("DELETE FROM books_genres");
        exec("DELETE FROM books");
        exec("DELETE FROM genres");
        exec("DELETE FROM authors");

        exec("ALTER TABLE authors ALTER COLUMN id RESTART WITH 1");
        exec("ALTER TABLE genres ALTER COLUMN id RESTART WITH 1");
        exec("ALTER TABLE books ALTER COLUMN id RESTART WITH 1");
        exec("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
    }

    public void seedBasic() {
        Long a1 = insertAuthor("Author_1");
        Long a2 = insertAuthor("Author_2");
        Long a3 = insertAuthor("Author_3");

        Long g1 = insertGenre("Genre_1");
        Long g2 = insertGenre("Genre_2");
        Long g3 = insertGenre("Genre_3");
        Long g4 = insertGenre("Genre_4");
        Long g5 = insertGenre("Genre_5");
        Long g6 = insertGenre("Genre_6");

        Long b1 = insertBook("BookTitle_1", a1);
        Long b2 = insertBook("BookTitle_2", a2);
        Long b3 = insertBook("BookTitle_3", a3);

        linkBookGenre(b1, g1);
        linkBookGenre(b1, g2);
        linkBookGenre(b2, g3);
        linkBookGenre(b2, g4);
        linkBookGenre(b3, g5);
        linkBookGenre(b3, g6);
    }

    public void resetAndSeed() {
        cleanAll();
        seedBasic();
    }


    private Long insertAuthor(String fullName) {
        return ops.getDatabaseClient()
                .sql("insert into authors(full_name) values (:fullName)")
                .bind("fullName", fullName)
                .filter((st, exec) -> st.returnGeneratedValues("id").execute())
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
    }

    private Long insertGenre(String name) {
        return ops.getDatabaseClient()
                .sql("insert into genres(name) values (:name)")
                .bind("name", name)
                .filter((st, exec) -> st.returnGeneratedValues("id").execute())
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
    }

    private Long insertBook(String title, Long authorId) {
        return ops.getDatabaseClient()
                .sql("insert into books(title, author_id) values (:title, :authorId)")
                .bind("title", title)
                .bind("authorId", authorId)
                .filter((st, exec) -> st.returnGeneratedValues("id").execute())
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
    }

    private void linkBookGenre(Long bookId, Long genreId) {
        ops.getDatabaseClient()
                .sql("insert into books_genres(book_id, genre_id) values (:b, :g)")
                .bind("b", bookId)
                .bind("g", genreId)
                .fetch()
                .rowsUpdated()
                .block();
    }

    private void exec(String sql) {
        ops.getDatabaseClient().sql(sql).fetch().rowsUpdated().block();
    }
}
