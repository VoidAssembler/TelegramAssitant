package com.telegram.assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Компонент для повторной загрузки конфигурации с задержкой.
 * Пытается загрузить конфигурацию до тех пор, пока она не будет успешно получена.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryableConfigLoader {

    private static final long RETRY_DELAY_MINUTES = 5;

    /**
     * Асинхронно загружает конфигурацию с повторными попытками.
     *
     * @param configKey ключ конфигурации
     * @param loader функция загрузки конфигурации
     * @return CompletableFuture с загруженным значением
     */
    @Async
    public CompletableFuture<String> loadWithRetry(String configKey, Supplier<String> loader) {
        return CompletableFuture.supplyAsync(() -> {
            String value = null;
            int attempt = 1;

            while (value == null || value.isEmpty()) {
                try {
                    value = loader.get();
                    if (value == null || value.isEmpty()) {
                        log.error("Не удалось загрузить конфигурацию '{}' (попытка: {}). Следующая попытка через {} минут",
                                configKey, attempt, RETRY_DELAY_MINUTES);
                        TimeUnit.MINUTES.sleep(RETRY_DELAY_MINUTES);
                        attempt++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Прервано ожидание повторной попытки загрузки конфигурации", e);
                } catch (Exception e) {
                    log.error("Ошибка при загрузке конфигурации '{}': {}", configKey, e.getMessage());
                    try {
                        TimeUnit.MINUTES.sleep(RETRY_DELAY_MINUTES);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Прервано ожидание повторной попытки загрузки конфигурации", ie);
                    }
                    attempt++;
                }
            }
            
            log.info("Успешно загружена конфигурация '{}' (попытка: {})", configKey, attempt);
            return value;
        });
    }
}
