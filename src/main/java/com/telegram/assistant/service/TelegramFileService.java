package com.telegram.assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbstractTelegramBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Сервис для работы с файлами Telegram.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFileService extends DefaultAbstractTelegramBot {

    private final ConfigurationService configurationService;

    @Override
    public String getBotToken() {
        return configurationService.getValue("bot.token");
    }

    /**
     * Скачивает файл голосового сообщения.
     *
     * @param voice объект голосового сообщения
     * @return массив байтов с содержимым файла
     * @throws IOException если произошла ошибка при скачивании файла
     */
    public byte[] downloadVoiceFile(Voice voice) throws IOException {
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(voice.getFileId());
            
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            String fileUrl = file.getFileUrl(getBotToken());
            
            try (InputStream is = new URL(fileUrl).openStream()) {
                return is.readAllBytes();
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при получении файла из Telegram", e);
            throw new IOException("Не удалось получить файл из Telegram", e);
        }
    }
}
