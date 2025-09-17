package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.domain.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = StudentServiceImpl.class)
class StudentServiceImplTest {

    private static final String KEY_FIRST_NAME = "StudentService.input.first.name";
    private static final String KEY_LAST_NAME = "StudentService.input.last.name";

    @MockitoBean
    private LocalizedIOService ioService;

    @Autowired
    private StudentServiceImpl service;

    @Test
    @DisplayName("should ask first and last name and return Student")
    void shouldAskFirstAndLastNameAndReturnStudent() {
        // given
        when(ioService.readStringWithPromptLocalized(KEY_FIRST_NAME)).thenReturn("Bruce");
        when(ioService.readStringWithPromptLocalized(KEY_LAST_NAME)).thenReturn("Wayne");

        // when
        Student student = service.determineCurrentStudent();

        // then
        assertThat(student.getFullName()).isEqualTo("Bruce Wayne");
        verify(ioService).readStringWithPromptLocalized(KEY_FIRST_NAME);
        verify(ioService).readStringWithPromptLocalized(KEY_LAST_NAME);
        verifyNoMoreInteractions(ioService);
    }

}
