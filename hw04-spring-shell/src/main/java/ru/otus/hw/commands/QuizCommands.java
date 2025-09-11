package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.ResultService;
import ru.otus.hw.service.StudentService;
import ru.otus.hw.service.TestService;
import ru.otus.hw.shell.UserContext;

@ShellComponent
@RequiredArgsConstructor
public class QuizCommands {

    private final UserContext userContext;

    private final StudentService studentService;

    private final TestService testService;

    private final ResultService resultService;

    private final LocalizedIOService io;

    private final AppProperties appProperties;

    @ShellMethod(key = {"login", "l"}, value = "Login: login --first-name John --last-name Doe")
    public void login(@ShellOption({"--first-name", "-f"}) String firstName,
                      @ShellOption({"--last-name", "-l"}) String lastName) {
        var student = new Student(firstName, lastName);
        userContext.setStudent(student);
        io.printFormattedLineLocalized("ResultService.student", student.getFullName());
    }

    @ShellMethod(key = {"start", "s"}, value = "Start testing")
    @ShellMethodAvailability("startAvailability")
    public void start() {
        Student student = userContext.isLoggedIn()
                ? userContext.getStudent()
                : studentService.determineCurrentStudent();

        userContext.setStudent(student);
        TestResult result = testService.executeTestFor(student);
        resultService.showResult(result);
    }

    @ShellMethod(key = {"set-locale"}, value = "Set application locale tag: en-US or ru-RU")
    public void setLocale(@ShellOption({"--tag", "-t"}) String localeTag) {
        appProperties.setLocale(localeTag);
        io.printFormattedLineLocalized("Shell.locale.changed", localeTag);
    }

    public Availability startAvailability() {
        return userContext.isLoggedIn()
                ? Availability.available()
                : Availability.unavailable(io.getMessage("Shell.login.required"));
    }
}
