package ru.practicum.main.dto.mappers;

import org.mapstruct.Mapper;
import ru.practicum.main.dto.Location;
import ru.practicum.main.model.LocationEntity;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationEntity toLocation(Location location);

    Location toLocationDto(LocationEntity locationEntity);
}