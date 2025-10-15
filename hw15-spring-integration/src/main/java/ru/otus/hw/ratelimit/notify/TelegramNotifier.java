package ru.otus.hw.ratelimit.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.hw.ratelimit.domain.Incident;

@Slf4j
@Component
public class TelegramNotifier implements Notifier {

    @Override
    public void notify(Incident inc) {
        log.info("[TELEGRAM] Key={}, count={}, window=[{}..{}], routes={}, status={}",
                inc.correlationKey(), inc.count(), inc.windowStart(), inc.windowEnd(),
                inc.sampleRoutes(), inc.statusHistogram());
    }
}
