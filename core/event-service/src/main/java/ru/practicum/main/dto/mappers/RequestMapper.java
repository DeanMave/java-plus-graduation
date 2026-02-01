package ru.practicum.main.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.dto.response.request.ParticipationRequestDto;
import ru.practicum.main.dto.response.request.RequestDto;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "status", expression = "java(request.getStatus().name())")
    ParticipationRequestDto toParticipationRequestDto(RequestDto request);

    List<ParticipationRequestDto> toDto(List<RequestDto> requests);
}