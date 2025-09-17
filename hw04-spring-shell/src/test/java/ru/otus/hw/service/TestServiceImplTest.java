package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = TestServiceImpl.class)
class TestServiceImplTest {

    private static final String KEY_HEADER = "TestService.answer.the.questions";
    private static final String KEY_PROMPT = "TestService.enter.option";
    private static final String KEY_INVALID = "TestService.invalid.option";

    private static final int ANSWERS_COUNT = 3;

    @Autowired
    private TestServiceImpl service;

    @MockitoBean
    private LocalizedIOService io;

    @MockitoBean
    private QuestionDao dao;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("John", "Doe");
    }

    @Test
    @DisplayName("prints header and questions with options in declared order (visual contract)")
    void printsQuestionsAndOptionsInOrder() {
        // given
        when(dao.findAll()).thenReturn(createSampleQuestions());
        when(io.readIntForRangeWithPromptLocalized(eq(1), eq(ANSWERS_COUNT), eq(KEY_PROMPT), eq(KEY_INVALID)))
                .thenReturn(1, 1);

        // when
        service.executeTestFor(student);

        // then
        InOrder inOrder = inOrder(io);

        inOrder.verify(io).printLine("");
        inOrder.verify(io).printLineLocalized(KEY_HEADER);
        inOrder.verify(io).printLine("");

        // Q1
        inOrder.verify(io).printFormattedLine("%d) %s", 1, "Is there life on Mars?");
        inOrder.verify(io).printFormattedLine("   %d) %s", 1, "Science doesn't know this yet");
        inOrder.verify(io).printFormattedLine("   %d) %s", 2, "Certainly. The red UFO is from Mars. And green is from Venus");
        inOrder.verify(io).printFormattedLine("   %d) %s", 3, "Absolutely not");

        verify(io, atLeastOnce()).readIntForRangeWithPromptLocalized(eq(1), eq(ANSWERS_COUNT), eq(KEY_PROMPT), eq(KEY_INVALID));

        // Q2
        inOrder.verify(io).printFormattedLine("%d) %s", 2, "How should resources be loaded form jar in Java?");
        inOrder.verify(io).printFormattedLine("   %d) %s", 1, "ClassLoader#geResourceAsStream or ClassPathResource#getInputStream");
        inOrder.verify(io).printFormattedLine("   %d) %s", 2, "ClassLoader#geResource#getFile + FileReader");
        inOrder.verify(io).printFormattedLine("   %d) %s", 3, "Wingardium Leviosa");

        verify(io, atLeast(2)).readIntForRangeWithPromptLocalized(eq(1), eq(ANSWERS_COUNT), eq(KEY_PROMPT), eq(KEY_INVALID));
        verifyNoMoreInteractions(io);
    }

    @Test
    @DisplayName("computes right answers based on user choices")
    void computesRightAnswersFromUserChoices() {
        // given
        when(dao.findAll()).thenReturn(createSampleQuestions());
        when(io.readIntForRangeWithPromptLocalized(eq(1), eq(ANSWERS_COUNT), eq(KEY_PROMPT), eq(KEY_INVALID)))
                .thenReturn(1, 2);

        // when
        TestResult result = service.executeTestFor(student);

        // then
        assertThat(result.getStudent().getFullName()).isEqualTo("John Doe");
        assertThat(result.getAnsweredQuestions()).hasSize(2);
        assertThat(result.getRightAnswersCount()).isEqualTo(1);

        verify(dao).findAll();
        verify(io, atLeast(2)).readIntForRangeWithPromptLocalized(eq(1), eq(ANSWERS_COUNT), eq(KEY_PROMPT), eq(KEY_INVALID));
        verifyNoMoreInteractions(dao);
    }

    @Test
    @DisplayName("returns empty result and prints only header when there are no questions")
    void returnsEmptyResultWhenNoQuestions() {
        // given
        when(dao.findAll()).thenReturn(List.of());

        // when
        TestResult result = service.executeTestFor(student);

        // then
        InOrder inOrder = inOrder(io);
        inOrder.verify(io).printLine("");
        inOrder.verify(io).printLineLocalized(KEY_HEADER);
        inOrder.verify(io).printLine("");

        assertThat(result.getAnsweredQuestions()).isEmpty();
        assertThat(result.getRightAnswersCount()).isZero();

        verify(dao).findAll();
        verifyNoMoreInteractions(io, dao);
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