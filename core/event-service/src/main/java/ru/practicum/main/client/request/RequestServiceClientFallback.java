package ru.practicum.main.client.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.response.request.ConfirmedRequestsCountDto;
import ru.practicum.main.dto.response.request.RequestDto;
import ru.practicum.main.dto.response.request.RequestStatusUpdateDto;
import ru.practicum.main.exception.NotFoundException;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RequestServiceClientFallback implements RequestClient {

    @Override
    public Integer countConfirmedRequestsByEventId(Long eventId) {
        log.warn("Сервис заявок недоступен, возвращаем 0 подтверждённых заявок для события eventId={}", eventId);
        return 0;
    }

    @Override
    public List<ConfirmedRequestsCountDto> countConfirmedRequestsByEventIds(List<Long> eventIds) {
        log.warn("Сервис заявок недоступен, возвращаем пустой список для идентификаторов событий eventIds={}", eventIds);
        return Collections.emptyList();
    }

    @Override
    public List<RequestDto> getRequestsByEventId(Long eventId) {
        log.warn("Сервис заявок недоступен, возвращаем пустой список для события eventId={}", eventId);
        return Collections.emptyList();
    }

    @Override
    public List<RequestDto> findAllByRequesterId(Long requesterId) {
        log.warn("Сервис заявок недоступен, возвращаем пустой список для инициатора requesterId={}", requesterId);
        return Collections.emptyList();
    }

    @Override
    public Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId) {
        log.warn("Сервис заявок недоступен, возвращаем false для инициатора requesterId={} и события eventId={}", requesterId, eventId);
        return false;
    }

    @Override
    public Integer countByEventId(Long eventId) {
        log.warn("Сервис заявок недоступен, возвращаем 0 для события eventId={}", eventId);
        return 0;
    }

    @Override
    public List<RequestDto> findAllByIdInAndEventId(List<Long> ids, Long eventId) {
        log.warn("Сервис заявок недоступен, возвращаем пустой список для заявок с id={}, eventId={}", ids, eventId);
        return Collections.emptyList();
    }

    @Override
    public void updateRequestsStatus(RequestStatusUpdateDto updateDto) {
        log.warn("Сервис заявок недоступен, невозможно обновить статус заявок: requestIds={}, status={}",
                updateDto != null ? updateDto.getRequestIds() : "null",
                updateDto != null ? updateDto.getStatus() : "null");

        throw new NotFoundException("Сервис заявок недоступен");
    }
}