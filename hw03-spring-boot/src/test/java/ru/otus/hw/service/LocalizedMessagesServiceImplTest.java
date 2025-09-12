package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import ru.otus.hw.config.LocaleConfig;

import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalizedMessagesServiceImplTest {

    @Mock
    private LocaleConfig localeConfig;

    @Nested
    class English {
        @Test
        @DisplayName("should resolve EN message with args")
        void enMessage() {
            when(localeConfig.getLocale()).thenReturn(Locale.forLanguageTag("en-US"));

            var service = new LocalizedMessagesServiceImpl(localeConfig, createMessageSource());

            var text = service.getMessage("ResultService.student", "John Doe");
            assertThat(text).isEqualTo("Student: John Doe");
        }
    }

    @Nested
    class Russian {
        @Test
        @DisplayName("should resolve RU message with args")
        void ruMessage() {
            when(localeConfig.getLocale()).thenReturn(Locale.forLanguageTag("ru-RU"));

            var service = new LocalizedMessagesServiceImpl(localeConfig, createMessageSource());

            var text = service.getMessage("ResultService.student", "Иван Иванов");
            assertThat(text).isEqualTo("Студент: Иван Иванов");
        }
    }

    @Nested
    class Fallback {
        @Test
        @DisplayName("should fallback to default bundle when locale not found and fallbackToSystemLocale=false")
        void fallbackToDefault() {
            when(localeConfig.getLocale()).thenReturn(Locale.forLanguageTag("fr-FR"));

            var service = new LocalizedMessagesServiceImpl(localeConfig, createMessageSource());

            var text = service.getMessage("ResultService.test.results");
            assertThat(text).isEqualTo("Test results:");
        }
    }

    private static ResourceBundleMessageSource createMessageSource() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
