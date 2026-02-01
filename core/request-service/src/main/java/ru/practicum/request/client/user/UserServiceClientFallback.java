package ru.practicum.request.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.request.dto.response.user.UserDto;

import java.util.List;

@Slf4j
@Component
public class UserServiceClientFallback implements UserClient {
    @Override
    public UserDto getUserById(Long id) {
        log.error("Не удалось получить пользователя с id={} из сервиса пользователей", id);
        throw new RuntimeException("Сервис пользователей недоступен");
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids) {
        log.error("Не удалось получить пользователей с ids={} из сервиса пользователей", ids);
        throw new RuntimeException("Сервис пользователей недоступен");
    }
}