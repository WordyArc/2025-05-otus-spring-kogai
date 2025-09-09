package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestConfig;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ResultServiceImplTest {

    @Mock
    private TestConfig config;

    @Mock
    private IOService io;

    @InjectMocks
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
        verify(io).printLine("Test results: ");
        verify(io, atLeast(3)).printFormattedLine(anyString(), any());
        verify(io).printLine("Congratulations! You passed test!");
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
        verify(io).printLine("Test results: ");
        verify(io, atLeast(3)).printFormattedLine(anyString(), any());
        verify(io).printLine("Sorry. You fail test.");
        verifyNoMoreInteractions(io);
    }

}
