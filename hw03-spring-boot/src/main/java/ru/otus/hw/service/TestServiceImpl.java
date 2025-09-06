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

    private static final String QUESTION_KEY = "TestService.answer.the.questions";

    private static final String ENTER_OPTION_KEY = "TestService.enter.option";

    private static final String INVALID_OPTION_KEY = "TestService.invalid.option";

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLineLocalized(QUESTION_KEY);
        ioService.printLine("");

        final List<Question> questions = questionDao.findAll();
        final var result = new TestResult(student);
        if (questions.isEmpty()) {
            return result;
        }
        for (int i = 0; i < questions.size(); i++) {
            final var question = questions.get(i);
            printQuestion(question, i + 1);

            final List<Answer> answers = question.answers();
            printAnswers(answers);

            int choice = ioService.readIntForRangeWithPromptLocalized(
                    1, answers.size(), ENTER_OPTION_KEY, INVALID_OPTION_KEY
            );
            boolean isCorrect = answers.get(choice - 1).isCorrect();
            result.applyAnswer(question, isCorrect);
        }
        return result;
    }

    private void printQuestion(Question question, int number) {
        ioService.printFormattedLine("%d) %s", number, question.text());
    }

    private void printAnswers(List<Answer> answers) {
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("   %d) %s", i + 1, answers.get(i).text());
        }
    }

}
