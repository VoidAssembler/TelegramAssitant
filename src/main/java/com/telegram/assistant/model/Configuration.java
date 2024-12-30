package com.telegram.assistant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность конфигурации системы.
 * Хранит пары ключ-значение для настроек приложения с их описанием.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "configurations")
public class Configuration {
    /** Уникальный ключ конфигурации */
    @Id
    private String key;
    
    /** Значение конфигурации */
    private String value;
    
    /** Описание назначения конфигурации */
    private String description;
}
