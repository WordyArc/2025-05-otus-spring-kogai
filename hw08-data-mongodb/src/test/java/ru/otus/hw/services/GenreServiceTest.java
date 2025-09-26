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
import ru.otus.hw.models.Genre;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestDataConfig.class)
class GenreServiceTest extends CommonContext {

    @Autowired
    private GenreService genreService;

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
    @DisplayName("findAll returns all genres")
    void findAll() {
        var genres = genreService.findAll();
        assertThat(genres).extracting(Genre::getId)
                .containsExactlyInAnyOrder("g1", "g2", "g3", "g4", "g5", "g6");
        assertThat(genres).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
    }
}
