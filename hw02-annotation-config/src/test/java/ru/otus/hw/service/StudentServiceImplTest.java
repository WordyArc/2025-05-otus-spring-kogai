package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.domain.Student;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class StudentServiceImplTest {

    @Test
    @DisplayName("should ask first and last name and return Student")
    void shouldAskFirstAndLastNameAndReturnStudent() {
        // given
        var ioService = mock(IOService.class);
        when(ioService.readStringWithPrompt("Please input your first name")).thenReturn("Bruce");
        when(ioService.readStringWithPrompt("Please input your last name")).thenReturn("Wayne");
        var service = new StudentServiceImpl(ioService);

        // when
        Student student = service.determineCurrentStudent();

        // then
        assertThat(student.getFullName()).isEqualTo("Bruce Wayne");
        verify(ioService).readStringWithPrompt("Please input your first name");
        verify(ioService).readStringWithPrompt("Please input your last name");
        verifyNoMoreInteractions(ioService);
    }

}
