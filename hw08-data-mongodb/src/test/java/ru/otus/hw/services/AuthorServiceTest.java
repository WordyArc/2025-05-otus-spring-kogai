package ru.otus.hw.services;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.CommonContext;
import ru.otus.hw.TestData;
import ru.otus.hw.config.TestDataConfig;
import ru.otus.hw.models.Author;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
@Import(TestDataConfig.class)
class AuthorServiceTest extends CommonContext {

    @Autowired
    private AuthorService authorService;

    @Autowired
    protected TestData data;

    @BeforeEach
    void setUp() {
        data.resetAndSeed();
    }

    @AfterEach
    void tearDown() {
        data.cleanAll();
    }

    @Test
    @DisplayName("findAll returns all authors")
    void findAll() {
        var authors = authorService.findAll();
        assertThat(authors).extracting(Author::getId)
                .containsExactlyInAnyOrder("a1", "a2", "a3");
        assertThat(authors).extracting(Author::getFullName)
                .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3");
    }

}
