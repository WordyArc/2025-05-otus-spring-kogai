package ru.otus.hw.shell;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.otus.hw.domain.Student;

@Getter
@Setter
@Component
public class UserContext {
    private Student student;

    public boolean isLoggedIn() {
        return student != null;
    }
}
