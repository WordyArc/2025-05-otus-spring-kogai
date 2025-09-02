package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("TestServiceImpl")
class TestServiceImplTest {

    private IOService ioService;
    private TestServiceImpl testService; // SUT

    @BeforeEach
    void setUp() {
        // given
        ioService = mock(IOService.class);
        var questionDao = mock(QuestionDao.class);

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

        when(questionDao.findAll()).thenReturn(List.of(q1, q2));

        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    @DisplayName("should print questions and answer options in the declared order")
    void shouldPrintQuestionsAndAnswerOptionsInDeclaredOrder() {
        // when
        testService.executeTest();

        // then
        InOrder inOrder = inOrder(ioService);

        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below");

        // Q1
        inOrder.verify(ioService).printFormattedLine("%d) %s", 1, "Is there life on Mars?");
        inOrder.verify(ioService).printFormattedLine("   %c. %s", 'a', "Science doesn't know this yet");
        inOrder.verify(ioService).printFormattedLine(
                "   %c. %s",
                'b',
                "Certainly. The red UFO is from Mars. And green is from Venus"
        );
        inOrder.verify(ioService).printFormattedLine("   %c. %s", 'c', "Absolutely not");

        // Q2
        inOrder.verify(ioService).printFormattedLine(
                "%d) %s",
                2,
                "How should resources be loaded form jar in Java?"
        );
        inOrder.verify(ioService).printFormattedLine(
                "   %c. %s",
                'a',
                "ClassLoader#geResourceAsStream or ClassPathResource#getInputStream"
        );
        inOrder.verify(ioService).printFormattedLine(
                "   %c. %s",
                'b',
                "ClassLoader#geResource#getFile + FileReader"
        );
        inOrder.verify(ioService).printFormattedLine("   %c. %s", 'c', "Wingardium Leviosa");

        verifyNoMoreInteractions(ioService);
    }

    @Test
    @DisplayName("should print only the header when there are no questions")
    void shouldPrintOnlyHeaderWhenNoQuestions() {
        // given
        IOService localIo = mock(IOService.class);
        var emptyDao = mock(QuestionDao.class);
        when(emptyDao.findAll()).thenReturn(List.of());
        var localService = new TestServiceImpl(localIo, emptyDao);

        // when
        localService.executeTest();

        // then
        InOrder inOrder = inOrder(localIo);
        inOrder.verify(localIo).printLine("");
        inOrder.verify(localIo).printFormattedLine("Please answer the questions below");
        verifyNoMoreInteractions(localIo);
    }
}