package ru.practicum.comment.dto.mappers;

import org.mapstruct.Mapper;
import ru.practicum.comment.dto.response.user.UserDto;
import ru.practicum.comment.dto.response.user.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserShortDto toUserShortDto(UserDto user);
}