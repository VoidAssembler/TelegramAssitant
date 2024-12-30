package com.telegram.assistant.config;

import com.telegram.assistant.service.ConfigurationService;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Конфигурация Telegram бота.
 * Загружает настройки бота из базы данных через ConfigurationService.
 */
@Configuration
@Component
@Getter
public class BotConfig {
    private final String token;
    private final String username;

    public BotConfig(ConfigurationService configurationService) {
        this.token = configurationService.getValue("bot.token");
        this.username = configurationService.getValue("bot.username");
    }
}
