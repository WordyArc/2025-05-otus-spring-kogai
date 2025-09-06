package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalizedIOServiceImplTest {

    private static final String ENTER_OPTION_KEY = "TestService.enter.option";
    private static final String INVALID_OPTION_KEY = "TestService.invalid.option";

    @Mock
    private IOService ioService;

    @Mock
    private LocalizedMessagesService messagesService;

    @InjectMocks
    private LocalizedIOServiceImpl localizedIoService;

    @Test
    @DisplayName("should formats prompt via MessageSource with {0}-{1}")
    void shouldFormatPromptWithTwoArguments() {
        int min = 1, max = 3;

        when(messagesService.getMessage(ENTER_OPTION_KEY, min, max))
                .thenReturn("Enter option number (1-3):");
        when(messagesService.getMessage(INVALID_OPTION_KEY))
                .thenReturn("Invalid option. Try again");

        localizedIoService.readIntForRangeWithPromptLocalized(min, max, ENTER_OPTION_KEY, INVALID_OPTION_KEY);

        verify(ioService).readIntForRangeWithPrompt(
                eq(min), eq(max),
                eq("Enter option number (1-3):"), eq("Invalid option. Try again")
        );
        verifyNoMoreInteractions(ioService);
    }

}
