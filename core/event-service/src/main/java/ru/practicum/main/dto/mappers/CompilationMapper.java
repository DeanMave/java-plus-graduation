package ru.practicum.main.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.main.dto.request.compilation.NewCompilationDto;
import ru.practicum.main.dto.response.compilation.CompilationDto;
import ru.practicum.main.dto.response.event.EventShortDto;
import ru.practicum.main.dto.response.user.UserDto;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = {EventMapper.class})
public abstract class CompilationMapper {

    @Autowired
    protected EventMapper eventMapper;

    @Mapping(target = "events", source = "events")
    @Mapping(target = "id", ignore = true)
    public abstract Compilation toEntity(NewCompilationDto newCompilation, Set<Event> events);

    public CompilationDto toDto(Compilation compilation, Map<Long, UserDto> usersMap) {
        if (compilation == null) return null;

        Set<EventShortDto> eventDtos = compilation.getEvents().stream()
                .map(event -> {
                    UserDto userDto = usersMap.get(event.getInitiatorId());
                    if (userDto == null) {
                        throw new NotFoundException("Пользователь не найден для события " + event.getId());
                    }
                    return eventMapper.toEventShortDto(event, userDto);
                })
                .collect(Collectors.toSet());

        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(eventDtos)
                .build();
    }

    public List<CompilationDto> toDtoList(List<Compilation> compilations, Map<Long, UserDto> usersMap) {
        if (compilations == null) return null;
        return compilations.stream()
                .map(c -> toDto(c, usersMap))
                .collect(Collectors.toList());
    }
}