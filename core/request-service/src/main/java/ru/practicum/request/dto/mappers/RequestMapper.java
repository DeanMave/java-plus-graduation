package ru.practicum.request.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.request.dto.response.request.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

import ru.practicum.request.dto.response.request.RequestDto;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    List<ParticipationRequestDto> toParticipationRequestDtoList(List<Request> requests);

    RequestDto toRequestDto(Request request);

    List<RequestDto> toRequestDtoList(List<Request> requests);
}