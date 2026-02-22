package ru.practicum.main.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.main.dto.request.event.NewEventDto;
import ru.practicum.main.dto.response.event.EventFullDto;
import ru.practicum.main.dto.response.event.EventShortDto;
import ru.practicum.main.dto.response.user.UserDto;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.LocationEntity;


@Mapper(componentModel = "spring", uses = {CategoryMapper.class, LocationMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "initiator", source = "userDto")
    @Mapping(target = "confirmedRequests", source = "event.confirmedRequests")
    @Mapping(target = "rating", ignore = true)
    EventShortDto toEventShortDto(Event event, UserDto userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "locationEntity", source = "savedLocationEntity")
    @Mapping(target = "initiatorId", source = "userId")
    @Mapping(target = "paid", source = "newEventDto.paid", defaultValue = "false")
    @Mapping(target = "participantLimit", source = "newEventDto.participantLimit", defaultValue = "0")
    @Mapping(target = "requestModeration", source = "newEventDto.requestModeration", defaultValue = "true")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "confirmedRequests", constant = "0")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    Event toEventFromNewEventDto(NewEventDto newEventDto, Long userId, Category category, LocationEntity savedLocationEntity);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "userDto")
    @Mapping(target = "location", source = "event.locationEntity")
    @Mapping(target = "rating", ignore = true)
    EventFullDto toEventFullDto(Event event, UserDto userDto);
}