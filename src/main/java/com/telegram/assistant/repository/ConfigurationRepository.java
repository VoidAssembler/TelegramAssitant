package com.telegram.assistant.repository;

import com.telegram.assistant.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с конфигурациями системы.
 * Предоставляет методы для взаимодействия с таблицей конфигураций в базе данных.
 */
public interface ConfigurationRepository extends JpaRepository<Configuration, String> {
}
