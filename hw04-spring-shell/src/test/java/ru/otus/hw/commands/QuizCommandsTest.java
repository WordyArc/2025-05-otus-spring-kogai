package ru.otus.hw.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.ResultService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestService;
import ru.otus.hw.shell.UserContext;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {
        "spring.shell.interactive.enabled=false",
        "spring.shell.script.enabled=false"
})
@SpringBootTest(classes = {QuizCommands.class, UserContext.class})
class QuizCommandsTest {

    @Autowired
    private QuizCommands commands;

    @Autowired
    private UserContext userContext;

    @MockitoBean
    private LocalizedIOService io;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private TestService testService;

    @MockitoBean
    private ResultService resultService;

    @MockitoBean
    private AppProperties properties;

    @BeforeEach
    void setUp() {
        when(properties.getLocale()).thenReturn(Locale.forLanguageTag("en-US"));
        when(properties.getRightAnswersCountToPass()).thenReturn(3);
    }

    @Test
    @DisplayName("login prints localized student name")
    void loginPrintsStudent() {
        commands.login("Bruce", "Wayne");
        verify(io).printFormattedLineLocalized(eq("ResultService.student"), eq("Bruce Wayne"));
    }

    @Test
    @DisplayName("start uses student from context")
    void startUsesContextStudent() {
        var s = new Student("Ivanov", "Ivan");
        userContext.setStudent(s);
        when(testService.executeTestFor(s)).thenReturn(new TestResult(s));

        commands.start();

        verify(testService).executeTestFor(s);
        verify(resultService).showResult(any(TestResult.class));
        verifyNoInteractions(studentService);
    }

    @Test
    @DisplayName("start asks student when not logged in")
    void startAsksStudentWhenNotLogged() {
        var s = new Student("Tony", "Stark");
        when(studentService.determineCurrentStudent()).thenReturn(s);
        when(testService.executeTestFor(s)).thenReturn(new TestResult(s));

        commands.start();

        verify(studentService).determineCurrentStudent();
        verify(testService).executeTestFor(s);
        verify(resultService).showResult(any(TestResult.class));
    }

}
