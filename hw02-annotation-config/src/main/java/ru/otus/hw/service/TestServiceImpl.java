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

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        List<Question> questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (int i = 0; i < questions.size(); i++) {
            var question = questions.get(i);
            ioService.printFormattedLine("%d) %s", i + 1, question.text());

            List<Answer> answers = question.answers();
            for (int j = 0; j < answers.size(); j++) {
                ioService.printFormattedLine("   %d) %s", j + 1, answers.get(j).text());
            }

            int choice = ioService.readIntForRangeWithPrompt(
                    1,
                    answers.size(),
                    "Enter option number (1-" + answers.size() + "):",
                    "Invalid option. Try again"
            );
            boolean isRight = answers.get(choice - 1).isCorrect();
            testResult.applyAnswer(question, isRight);
        }
        return testResult;
    }
}
