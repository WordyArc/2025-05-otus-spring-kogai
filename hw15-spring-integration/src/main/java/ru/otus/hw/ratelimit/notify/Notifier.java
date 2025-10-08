package ru.otus.hw.ratelimit.notify;

import ru.otus.hw.ratelimit.domain.Incident;

public interface Notifier {
    void notify(Incident incident);
}
