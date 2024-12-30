package com.telegram.assistant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Сущность пользователя Telegram бота.
 * Хранит информацию о пользователе, включая его идентификатор чата и персональные данные.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {
    /** Уникальный идентификатор чата пользователя в Telegram */
    @Id
    private Long chatId;
    
    /** Имя пользователя в Telegram */
    private String username;
    
    /** Имя пользователя */
    private String firstName;
    
    /** Фамилия пользователя */
    private String lastName;
    
    /** Дата и время регистрации пользователя */
    private LocalDateTime registrationDate;
    
    /** Флаг активности пользователя */
    private boolean active;
}
