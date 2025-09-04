package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.dao.dto.AnswerCsvConverter;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvQuestionDaoTest {

    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.setTestFileName("questions-test.csv");
    }

    @Test
    @DisplayName("should read questions from classpath resource and map to domain model")
    void shouldReadQuestionsFromResource() {
        // given
        var dao = new CsvQuestionDao(appProperties);

        // when
        List<Question> questions = dao.findAll();

        // then
        assertThat(questions).isNotEmpty();
        assertThat(questions.get(0).text()).isNotBlank();
        assertThat(questions.get(0).answers()).isNotEmpty();
    }

}
