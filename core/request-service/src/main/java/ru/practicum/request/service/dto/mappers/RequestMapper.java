package ru.practicum.request.service.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.request.service.dto.response.request.ParticipationRequestDto;
import ru.practicum.request.service.model.Request;

import java.util.List;

import ru.practicum.request.service.dto.response.request.RequestDto;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    List<ParticipationRequestDto> toParticipationRequestDtoList(List<Request> requests);

    RequestDto toRequestDto(Request request);

    List<RequestDto> toRequestDtoList(List<Request> requests);
}