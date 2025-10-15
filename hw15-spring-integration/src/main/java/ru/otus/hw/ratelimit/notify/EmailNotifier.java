package ru.otus.hw.ratelimit.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.hw.ratelimit.domain.Incident;

@Slf4j
@Component
public class EmailNotifier implements Notifier {
    @Override
    public void notify(Incident inc) {
        log.info("[EMAIL] Incident {} for key {} ({} events)", inc.id(), inc.correlationKey(), inc.count());
    }
}
