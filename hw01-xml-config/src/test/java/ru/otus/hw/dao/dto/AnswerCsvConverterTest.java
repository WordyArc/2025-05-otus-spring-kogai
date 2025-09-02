package ru.otus.hw.dao.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.Answer;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerCsvConverterTest {

    @Test
    @DisplayName("should parse 'text%true' into Answer with isCorrect=true")
    void shouldParseTrueFlag() {
        // given
        var converter = new AnswerCsvConverter();
        var csv = "Science doesn't know this yet%true";

        // when
        Object result = converter.convertToRead(csv);

        // then
        var answer = (Answer) result;
        assertThat(answer.text()).isEqualTo("Science doesn't know this yet");
        assertThat(answer.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("should parse 'text%false' into Answer with isCorrect=false")
    void shouldParseFalseFlag() {
        // given
        var converter = new AnswerCsvConverter();
        var csv = "Absolutely not%false";

        // when
        Object result = converter.convertToRead(csv);

        // then
        var answer = (Answer) result;
        assertThat(answer.text()).isEqualTo("Absolutely not");
        assertThat(answer.isCorrect()).isFalse();
    }
}