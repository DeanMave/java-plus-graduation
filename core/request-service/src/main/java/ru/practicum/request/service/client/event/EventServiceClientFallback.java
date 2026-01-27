package ru.practicum.request.service.client.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.request.service.dto.response.event.EventDto;

@Slf4j
@Component
public class EventServiceClientFallback implements EventClient {
    @Override
    public EventDto getEventById(Long eventId) {
        log.error("Не удалось получить событие с id={} из сервиса событий", eventId);
        throw new RuntimeException("Сервис событий недоступен");
    }
}