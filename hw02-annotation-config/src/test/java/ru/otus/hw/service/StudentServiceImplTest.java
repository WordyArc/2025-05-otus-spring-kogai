package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.domain.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private IOService ioService;

    @InjectMocks
    private StudentServiceImpl service;

    @Test
    @DisplayName("should ask first and last name and return Student")
    void shouldAskFirstAndLastNameAndReturnStudent() {
        // given
        when(ioService.readStringWithPrompt("Please input your first name")).thenReturn("Bruce");
        when(ioService.readStringWithPrompt("Please input your last name")).thenReturn("Wayne");

        // when
        Student student = service.determineCurrentStudent();

        // then
        assertThat(student.getFullName()).isEqualTo("Bruce Wayne");
        verify(ioService).readStringWithPrompt("Please input your first name");
        verify(ioService).readStringWithPrompt("Please input your last name");
        verifyNoMoreInteractions(ioService);
    }

}
