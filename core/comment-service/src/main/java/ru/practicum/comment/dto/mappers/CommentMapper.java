package ru.practicum.comment.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comment.dto.request.comment.NewCommentDto;
import ru.practicum.comment.dto.response.comment.CommentDto;
import ru.practicum.comment.dto.response.user.UserDto;
import ru.practicum.comment.model.Comment;



@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "id", constant = "0L")
    Comment toEntity(NewCommentDto newCommentDto);

    @Mapping(source = "comment.id", target = "id")
    @Mapping(source = "userDto", target = "user")
    CommentDto toDto(Comment comment, UserDto userDto);
}