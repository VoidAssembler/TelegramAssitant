package com.telegram.assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.springframework.core.io.ByteArrayResource;

/**
 * Сервис для работы с Whisper API для преобразования голосовых сообщений в текст.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhisperService {

    private final RestTemplate restTemplate;
    private final TelegramFileService telegramFileService;
    private final ConfigurationService configurationService;

    /**
     * Преобразует голосовое сообщение в текст.
     *
     * @param voice голосовое сообщение
     * @return распознанный текст или сообщение об ошибке
     */
    public String transcribeVoice(Voice voice) {
        try {
            String whisperApiUrl = configurationService.getValue("whisper.api.url");
            if (whisperApiUrl == null || whisperApiUrl.isEmpty()) {
                throw new IllegalStateException("URL для Whisper API не настроен");
            }

            // Получаем файл голосового сообщения
            byte[] voiceData = telegramFileService.downloadVoiceFile(voice);
            
            // Подготавливаем данные для отправки
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource voiceResource = new ByteArrayResource(voiceData) {
                @Override
                public String getFilename() {
                    return "voice.oga";
                }
            };
            body.add("file", voiceResource);

            // Настраиваем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Отправляем запрос
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                whisperApiUrl + "/asr",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Ошибка при обработке голосового сообщения: {}", response.getStatusCode());
                return "Извините, не удалось распознать голосовое сообщение";
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке голосового сообщения", e);
            return "Произошла ошибка при обработке голосового сообщения";
        }
    }
}
