package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvQuestionDaoTest {

    @Test
    @DisplayName("should read questions from classpath resource and map to domain model")
    void shouldReadQuestionsFromResource() {
        // given
        var props = new AppProperties("questions-test.csv");
        var dao = new CsvQuestionDao(props);

        // when
        List<Question> questions = dao.findAll();

        // then
        assertThat(questions).isNotEmpty();
        assertThat(questions.get(0).text()).isNotBlank();
        assertThat(questions.get(0).answers()).isNotEmpty();
    }
}