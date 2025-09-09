package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvQuestionDaoTest {

    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.setTestFileName("questions-test.csv");
    }

    @Test
    @DisplayName("should map entire CSV to domain model exactly")
    void shouldMapCsvExactly() {
        // given
        var dao = new CsvQuestionDao(appProperties);

        // when
        List<Question> actual = dao.findAll();

        // then
        assertThat(actual).containsExactlyElementsOf(createSampleQuestions());
    }

    @Test
    @DisplayName("should throw QuestionReadException when CSV resource is missing")
    void shouldThrowWhenCsvMissing() {
        TestFileNameProvider missing = () -> "no_such_file.csv";
        var dao = new CsvQuestionDao(missing);

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("no_such_file.csv");
    }

    private List<Question> createSampleQuestions() {
        return List.of(
                new Question(
                        "Is there life on Mars?",
                        List.of(
                                new Answer("Science doesn't know this yet", true),
                                new Answer("Certainly. The red UFO is from Mars. And green is from Venus", false),
                                new Answer("Absolutely not", false)
                        )
                ),
                new Question(
                        "How should resources be loaded form jar in Java?",
                        List.of(
                                new Answer("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream", true),
                                new Answer("ClassLoader#geResource#getFile + FileReader", false),
                                new Answer("Wingardium Leviosa", false)
                        )
                ),
                new Question(
                        "Which option is a good way to handle the exception?",
                        List.of(
                                new Answer("@SneakyThrow", false),
                                new Answer("e.printStackTrace()", false),
                                new Answer("Rethrow with wrapping in business exception (for example, QuestionReadException)", true),
                                new Answer("Ignoring exception", false)
                        )
                )
        );
    }

}
