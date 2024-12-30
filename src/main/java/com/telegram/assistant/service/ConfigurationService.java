package com.telegram.assistant.service;

import com.telegram.assistant.model.Configuration;
import com.telegram.assistant.repository.ConfigurationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Сервис для работы с конфигурациями системы.
 * Обеспечивает загрузку и управление конфигурационными параметрами из базы данных.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final RetryableConfigLoader retryableConfigLoader;

    /**
     * Инициализация базовых конфигураций при старте приложения.
     * Создает записи с пустыми значениями, если они отсутствуют.
     */
    @PostConstruct
    public void init() {
        initConfigIfNotExists("bot.token", "Токен Telegram бота");
        initConfigIfNotExists("bot.username", "Имя пользователя Telegram бота");
        initConfigIfNotExists("whisper.api.url", "URL сервиса Whisper API для распознавания речи");
        initConfigIfNotExists("external.api.url", "URL внешнего API для обработки сообщений");
    }

    /**
     * Получает значение конфигурации по ключу с поддержкой повторных попыток.
     *
     * @param key ключ конфигурации
     * @return значение конфигурации
     */
    public String getValue(String key) {
        CompletableFuture<String> future = retryableConfigLoader.loadWithRetry(key, () -> 
            configurationRepository.findById(key)
                .map(Configuration::getValue)
                .orElse(null)
        );

        try {
            // Ждем результат не более 30 секунд при первой попытке
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Прервано ожидание загрузки конфигурации", e);
        } catch (ExecutionException | TimeoutException e) {
            log.warn("Не удалось получить значение конфигурации '{}' сразу, загрузка продолжится в фоновом режиме", key);
            // Возвращаем пустую строку, значение будет обновлено позже
            return "";
        }
    }

    /**
     * Создает новую конфигурацию, если она не существует.
     *
     * @param key ключ конфигурации
     * @param description описание конфигурации
     */
    private void initConfigIfNotExists(String key, String description) {
        if (!configurationRepository.existsById(key)) {
            Configuration config = new Configuration();
            config.setKey(key);
            config.setValue("");
            config.setDescription(description);
            configurationRepository.save(config);
            log.info("Создана новая конфигурация: {}", key);
        }
    }
}
