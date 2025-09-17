package ru.otus.hw.dao.dto;

import com.opencsv.bean.AbstractCsvConverter;
import ru.otus.hw.domain.Answer;

public class AnswerCsvConverter extends AbstractCsvConverter {

    @Override
    public Object convertToRead(String value) {
        if (value == null) {
            return new Answer("", false);
        }
        String[] parts = value.split("%", 2);
        String text = parts.length > 0 ? parts[0] : "";
        boolean isCorrect = parts.length > 1 && Boolean.parseBoolean(parts[1]);
        return new Answer(text, isCorrect);
    }
}
