package ru.practicum.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.mapper.UserActionMapper;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.UserInteractionRepository;
import ru.practicum.stats.avro.UserActionAvro;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class UserActionServiceImpl implements UserActionService {

    private final UserInteractionRepository userInteractionRepository;
    private final UserActionMapper mapper;

    @Override
    public void save(UserActionAvro userActionAvro) {
        log.info("Сохраняем действие {} пользователя с id {} для события c id {}",
                userActionAvro.getActionType(),
                userActionAvro.getUserId(),
                userActionAvro.getEventId());
        UserAction newUserAction = mapper.toEntity(userActionAvro);
        userInteractionRepository
                .findByUserIdAndEventId(newUserAction.getUserId(), newUserAction.getEventId())
                .ifPresentOrElse(
                        existingAction -> updateExistingAction(existingAction, newUserAction),
                        () -> createNewAction(newUserAction)
                );
    }

    private void updateExistingAction(UserAction existingAction, UserAction newAction) {
        if (newAction.getRating() > existingAction.getRating()) {
            log.debug("Обновляем рейтинг для пользователя {} и события {}: {} -> {}",
                    existingAction.getUserId(),
                    existingAction.getEventId(),
                    existingAction.getRating(),
                    newAction.getRating());

            existingAction.setRating(newAction.getRating());
            existingAction.setTimestamp(newAction.getTimestamp());
            userInteractionRepository.save(existingAction);
        } else {
            log.debug("Рейтинг не обновлен: текущий {} >= нового {} для пользователя {} и события {}",
                    existingAction.getRating(),
                    newAction.getRating(),
                    existingAction.getUserId(),
                    existingAction.getEventId());
        }
    }

    private void createNewAction(UserAction newAction) {
        log.debug("Создаем новую запись взаимодействия для пользователя {} и события {} с рейтингом {}",
                newAction.getUserId(),
                newAction.getEventId(),
                newAction.getRating());

        userInteractionRepository.save(newAction);
    }
}
