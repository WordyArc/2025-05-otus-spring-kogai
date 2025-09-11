package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestConfig;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ResultServiceImpl.class)
class ResultServiceImplTest {

    private static final String KEY_RESULTS = "ResultService.test.results";
    private static final String KEY_STUDENT = "ResultService.student";
    private static final String KEY_ANSWERED_QUESTIONS_COUNT = "ResultService.answered.questions.count";
    private static final String KEY_RIGHT_ANSWERS_COUNT = "ResultService.right.answers.count";
    private static final String KEY_PASSED = "ResultService.passed.test";
    private static final String KEY_FAILED = "ResultService.fail.test";

    @MockitoBean
    private TestConfig config;

    @MockitoBean
    private LocalizedIOService io;

    @Autowired
    private ResultServiceImpl service;

    @Test
    @DisplayName("prints PASS when right answers >= threshold")
    void printsPassWhenRightAnswersMeetThreshold() {
        // given
        when(config.getRightAnswersCountToPass()).thenReturn(2);
        var result = new TestResult(new Student("A", "B"));
        result.applyAnswer(new Question("Q1", List.of()), true);
        result.applyAnswer(new Question("Q2", List.of()), true);

        // when
        service.showResult(result);

        // then
        verify(io).printLine("");
        verify(io).printLineLocalized(KEY_RESULTS);
        verify(io).printFormattedLineLocalized(eq(KEY_STUDENT), any());
        verify(io).printFormattedLineLocalized(eq(KEY_ANSWERED_QUESTIONS_COUNT), any());
        verify(io).printFormattedLineLocalized(eq(KEY_RIGHT_ANSWERS_COUNT), any());
        verify(io).printLineLocalized(KEY_PASSED);
        verifyNoMoreInteractions(io);
    }

    @Test
    @DisplayName("prints FAIL when right answers < threshold")
    void printsFailWhenRightAnswersBelowThreshold() {
        // given
        when(config.getRightAnswersCountToPass()).thenReturn(2);
        var result = new TestResult(new Student("A", "B"));
        result.applyAnswer(new Question("Q1", List.of()), true);
        result.applyAnswer(new Question("Q2", List.of()), false);

        // when
        service.showResult(result);

        // then
        verify(io).printLine("");
        verify(io).printLineLocalized(KEY_RESULTS);
        verify(io).printFormattedLineLocalized(eq(KEY_STUDENT), any());
        verify(io).printFormattedLineLocalized(eq(KEY_ANSWERED_QUESTIONS_COUNT), any());
        verify(io).printFormattedLineLocalized(eq(KEY_RIGHT_ANSWERS_COUNT), any());
        verify(io).printLineLocalized(KEY_FAILED);
        verifyNoMoreInteractions(io);
    }

}
