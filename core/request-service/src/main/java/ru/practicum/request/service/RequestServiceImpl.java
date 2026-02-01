package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.request.client.event.EventClient;
import ru.practicum.request.client.user.UserClient;
import ru.practicum.request.dto.mappers.RequestMapper;
import ru.practicum.request.dto.response.event.EventDto;
import ru.practicum.request.dto.response.request.ParticipationRequestDto;
import ru.practicum.request.dto.response.user.UserDto;
import ru.practicum.request.exception.ConflictException;
import ru.practicum.request.exception.NotFoundException;
import ru.practicum.request.exception.OwnershipMismatchException;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.service.interfaces.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getRequestsByRequesterId(Long userId) {
        return requestMapper.toParticipationRequestDtoList(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUserById(userId);
        Request request = getRequestById(requestId);
        if (!request.getRequesterId().equals(userId)) {
            throw new OwnershipMismatchException("Пользователь " + userId + " не отправлял запрос " + request.getId());
        }
        request.setStatus(Request.RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Параметр eventId обязателен");
        }
        EventDto event = getEventById(eventId);
        UserDto user = getUserById(userId);
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос на участие в событии " + eventId + " уже создан");
        }
        if (event.getInitiatorId().equals(userId)) {
            throw new ConflictException(userId + " является инициатором события");
        }
        if (event.getState() != EventDto.EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (!event.getRequestModeration()) {
            int confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedRequests >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
                throw new ConflictException("Достигнут лимит подтверждённых участников");
            }
        }
        Request request = Request.builder()
                .requesterId(user.getId())
                .eventId(eventId)
                .created(LocalDateTime.now())
                .status(event.getRequestModeration() ? Request.RequestStatus.PENDING : Request.RequestStatus.CONFIRMED)
                .build();
        if (event.getParticipantLimit() == 0) {
            request.setStatus(Request.RequestStatus.CONFIRMED);
        }
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    private UserDto getUserById(Long userId) {
        try {
            UserDto user = userClient.getUserById(userId);
            log.debug("Получен существующий пользователь из user-service: {}", user);
            return user;
        } catch (Exception e) {
            log.debug("Не удалось получить пользователя из user-service: {}", e.getMessage());
            throw new NotFoundException("Пользователь с userId " + userId + " не найден");
        }
    }

    private EventDto getEventById(Long eventId) {
        try {
            EventDto event = eventClient.getEventById(eventId);
            log.debug("Получено существующее событие из event-service: {}", event);
            return event;
        } catch (Exception e) {
            log.debug("Не удалось получить событие из event-service: {}", e.getMessage());
            throw new NotFoundException("Событие с eventId " + eventId + " не найдено");
        }
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с id " + requestId + " не найден"));
    }
}
