package com.telegram.assistant.repository;

import com.telegram.assistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с данными пользователей.
 * Предоставляет методы для взаимодействия с таблицей пользователей в базе данных.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Проверяет существование пользователя по идентификатору чата.
     *
     * @param chatId идентификатор чата пользователя
     * @return true, если пользователь существует, false в противном случае
     */
    boolean existsByChatId(Long chatId);
}
