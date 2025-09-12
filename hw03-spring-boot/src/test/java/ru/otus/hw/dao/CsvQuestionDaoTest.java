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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvQuestionDaoTest {

    private AppProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AppProperties();
        properties.setLocale("en-US");
        properties.setFileNameByLocaleTag(Map.of(
                "ru-RU", "questions_ru.csv",
                "en-US", "questions.csv"
        ));
    }

    @Test
    @DisplayName("should read questions from classpath resource and map to domain model")
    void shouldReadQuestionsFromResource() {
        // given
        var dao = new CsvQuestionDao(properties);

        // when
        final List<Question> questions = dao.findAll();

        // then
        assertThat(questions).containsExactlyElementsOf(createSampleQuestions());
    }

    @Test
    @DisplayName("should throw QuestionReadException when CSV is missing")
    void missingCsv() {
        TestFileNameProvider p = () -> "no_such_file.csv";

        var dao = new CsvQuestionDao(p);

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("no_such_file.csv");
    }

    @Test
    @DisplayName("ru-RU locale maps to questions_ru.csv and parses Russian locale")
    void picksRussianCsv() {
        properties.setLocale("ru-RU");

        var dao = new CsvQuestionDao(properties);

        List<Question> questions = dao.findAll();

        assertThat(questions).isNotEmpty();
        assertThat(questions.get(0).text())
                .isEqualTo("Есть ли жизнь на Марсе?");
        assertThat(questions.get(0).answers()).isNotEmpty();
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
