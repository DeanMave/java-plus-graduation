package ru.practicum.main.dto.mappers;

import org.mapstruct.Mapper;
import ru.practicum.main.dto.response.user.UserDto;
import ru.practicum.main.dto.response.user.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserShortDto toUserShortDto(UserDto user);
}