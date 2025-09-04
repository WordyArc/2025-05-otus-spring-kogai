package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.inOrder;

class TestServiceImplTest {

    private IOService io;
    private QuestionDao dao;
    private TestServiceImpl service;

    private Student student;

    @BeforeEach
    void setUp() {
        // given
        io = mock(IOService.class);
        dao = mock(QuestionDao.class);
        service = new TestServiceImpl(io, dao);
        student = new Student("John", "Doe");

        when(dao.findAll()).thenReturn(createSampleQuestions());
        when(io.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1, 1);
    }

    @Test
    @DisplayName("prints header and questions with options in declared order (visual contract)")
    void printsQuestionsAndOptionsInOrder() {
        // when
        service.executeTestFor(student);

        // then
        InOrder inOrder = inOrder(io);

        inOrder.verify(io).printLine("");
        inOrder.verify(io).printFormattedLine("Please answer the questions below%n");

        // Q1
        inOrder.verify(io).printFormattedLine("%d) %s", 1, "Is there life on Mars?");
        inOrder.verify(io).printFormattedLine("   %d) %s", 1, "Science doesn't know this yet");
        inOrder.verify(io).printFormattedLine("   %d) %s", 2,
                "Certainly. The red UFO is from Mars. And green is from Venus");
        inOrder.verify(io).printFormattedLine("   %d) %s", 3, "Absolutely not");

        verify(io, atLeastOnce()).readIntForRangeWithPrompt(eq(1), eq(3), anyString(), anyString());

        // Q2
        inOrder.verify(io).printFormattedLine("%d) %s", 2, "How should resources be loaded form jar in Java?");
        inOrder.verify(io).printFormattedLine("   %d) %s", 1,
                "ClassLoader#geResourceAsStream or ClassPathResource#getInputStream");
        inOrder.verify(io).printFormattedLine("   %d) %s", 2,
                "ClassLoader#geResource#getFile + FileReader");
        inOrder.verify(io).printFormattedLine("   %d) %s", 3, "Wingardium Leviosa");


        verify(io, atLeast(2)).readIntForRangeWithPrompt(eq(1), eq(3), anyString(), anyString());

        verifyNoMoreInteractions(io);
    }

    @Test
    @DisplayName("computes right answers based on user choices")
    void computesRightAnswersFromUserChoices() {
        // given
        when(io.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1, 2);

        // when
        TestResult result = service.executeTestFor(student);

        // then
        assertThat(result.getStudent().getFullName()).isEqualTo("John Doe");
        assertThat(result.getAnsweredQuestions()).hasSize(2);
        assertThat(result.getRightAnswersCount()).isEqualTo(1);

        verify(dao).findAll();
        verify(io, atLeast(2)).readIntForRangeWithPrompt(eq(1), eq(3), anyString(), anyString());
        verifyNoMoreInteractions(dao);
    }

    @Test
    @DisplayName("returns empty result and prints only header when there are no questions")
    void returnsEmptyResultWhenNoQuestions() {
        // given
        IOService localIo = mock(IOService.class);
        QuestionDao emptyDao = mock(QuestionDao.class);
        when(emptyDao.findAll()).thenReturn(List.of());
        TestServiceImpl localSut = new TestServiceImpl(localIo, emptyDao);

        // when
        TestResult result = localSut.executeTestFor(student);

        // then
        InOrder inOrder = inOrder(localIo);
        inOrder.verify(localIo).printLine("");
        inOrder.verify(localIo).printFormattedLine("Please answer the questions below%n");
        assertThat(result.getAnsweredQuestions()).isEmpty();
        assertThat(result.getRightAnswersCount()).isZero();

        verifyNoMoreInteractions(localIo);
        verify(emptyDao).findAll();
        verifyNoMoreInteractions(emptyDao);
    }

    private static List<Question> createSampleQuestions() {
        var q1 = new Question(
                "Is there life on Mars?",
                List.of(
                        new Answer("Science doesn't know this yet", true),
                        new Answer("Certainly. The red UFO is from Mars. And green is from Venus", false),
                        new Answer("Absolutely not", false)
                )
        );

        var q2 = new Question(
                "How should resources be loaded form jar in Java?",
                List.of(
                        new Answer("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream", true),
                        new Answer("ClassLoader#geResource#getFile + FileReader", false),
                        new Answer("Wingardium Leviosa", false)
                )
        );
        return List.of(q1, q2);
    }
}