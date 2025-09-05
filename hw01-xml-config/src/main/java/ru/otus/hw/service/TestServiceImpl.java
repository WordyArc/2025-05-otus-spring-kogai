package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below");

        List<Question> questions = questionDao.findAll();
        for (int i = 0; i < questions.size(); i++) {
            var question = questions.get(i);
            ioService.printFormattedLine("%d) %s", i + 1, question.text());

            List<Answer> answers = question.answers();
            for (int j = 0; j < answers.size(); j++) {
                char letter = (char) ('a' + j);
                ioService.printFormattedLine("   %c. %s", letter, answers.get(j).text());
            }
        }
    }
}