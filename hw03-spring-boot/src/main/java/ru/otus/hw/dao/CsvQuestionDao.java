package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();

        try (var inputStream = new ClassPathResource(fileName).getInputStream();
             var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            List<QuestionDto> dtos = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withType(QuestionDto.class)
                    .withSeparator(';')
                    .withSkipLines(1)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .withThrowExceptions(true)
                    .build()
                    .parse();

            return dtos.stream().map(QuestionDto::toDomainObject).toList();
        } catch (IOException | RuntimeException e) {
            throw new QuestionReadException("Error reading questions from resource: " + fileName, e);
        }
    }

}
