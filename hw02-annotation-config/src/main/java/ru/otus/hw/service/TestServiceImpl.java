package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private static final String HEADER = "Please answer the questions below%n";

    private static final String INVALID_OPTION_MSG = "Invalid option. Try again";

    private static final String OPTION_PROMPT_TEMPLATE = "Enter option number (1-%d):";

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine(HEADER);

        final List<Question> questions = questionDao.findAll();
        final var testResult = new TestResult(student);
        if (questions.isEmpty()) {
            return testResult;
        }

        for (int q = 0; q < questions.size(); q++) {
            final var question = questions.get(q);
            printQuestion(question, q + 1);

            final List<Answer> answers = question.answers();
            printAnswers(answers);

            final int choice = promptChoice(answers.size());
            final boolean isCorrect = answers.get(choice - 1).isCorrect();

            testResult.applyAnswer(question, isCorrect);
        }

        return testResult;
    }

    private void printQuestion(Question question, int number) {
        ioService.printFormattedLine("%d) %s", number, question.text());
    }

    private void printAnswers(List<Answer> answers) {
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("   %d) %s", i + 1, answers.get(i).text());
        }
    }

    private int promptChoice(int answersCount) {
        return ioService.readIntForRangeWithPrompt(
                1,
                answersCount,
                String.format(OPTION_PROMPT_TEMPLATE, answersCount),
                INVALID_OPTION_MSG
        );
    }
}
