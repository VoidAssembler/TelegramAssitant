package com.telegram.assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Сервис для работы с внешним API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final RestTemplate restTemplate;
    private final ConfigurationService configurationService;

    /**
     * Обрабатывает текстовое сообщение через внешнее API.
     *
     * @param text текст для обработки
     * @return ответ от API или сообщение об ошибке
     */
    public String processText(String text) {
        try {
            String externalApiUrl = configurationService.getValue("external.api.url");
            if (externalApiUrl == null || externalApiUrl.isEmpty()) {
                throw new IllegalStateException("URL для внешнего API не настроен");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            
            HttpEntity<String> request = new HttpEntity<>(text, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                externalApiUrl + "/process",
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Ошибка при обработке текста: {}", response.getStatusCode());
                return "Извините, не удалось обработать ваше сообщение";
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке текста", e);
            return "Произошла ошибка при обработке вашего сообщения";
        }
    }
}
