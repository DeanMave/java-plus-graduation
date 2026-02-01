package ru.practicum.user.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.user.dto.request.NewUserRequest;
import ru.practicum.user.dto.response.UserDto;
import ru.practicum.user.dto.response.UserShortDto;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true) // id сгенерируется БД, при создании из Request игнорируем
    User toEntity(NewUserRequest newUserRequest);

    UserDto toDto(User user);

    UserShortDto toUserShortDto(User user);
}