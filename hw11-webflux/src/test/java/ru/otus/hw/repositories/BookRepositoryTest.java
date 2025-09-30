package ru.otus.hw.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import reactor.test.StepVerifier;
import ru.otus.hw.CommonContext;
import ru.otus.hw.TestData;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@AutoConfigureJson
class BookRepositoryTest extends CommonContext {

    @Autowired
    BookRepository repository;

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
    @DisplayName("findAggregateById returns book with author and genres")
    void shouldFindById() {
        repository.findAggregateById(1L)
                .as(StepVerifier::create)
                .assertNext(b -> {
                    assertThat(b.getTitle()).isEqualTo("BookTitle_1");
                    assertThat(b.getAuthor().getFullName()).isEqualTo("Author_1");
                    assertThat(b.getGenres()).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("findAllAggregates returns all books with relations")
    void shouldFindAll() {
        repository.findAllAggregates()
                .collectList()
                .as(StepVerifier::create)
                .assertNext(list -> {
                    assertThat(list).hasSize(3);
                    list.forEach(b -> {
                        assertThat(b.getAuthor().getFullName()).isNotBlank();
                        assertThat(b.getGenres()).isNotEmpty();
                    });
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteById removes a book")
    void deleteExisting() {
        repository.deleteById(1L)
                .then(repository.findAggregateById(1L))
                .as(StepVerifier::create)
                .verifyComplete();
    }

}
