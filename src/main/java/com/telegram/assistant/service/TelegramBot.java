package com.telegram.assistant.service;

import com.telegram.assistant.config.BotConfig;
import com.telegram.assistant.repository.UserRepository;
import com.telegram.assistant.service.external.ExternalApiService;
import com.telegram.assistant.service.whisper.WhisperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Основной класс Telegram бота, обрабатывающий входящие сообщения и команды.
 * Расширяет TelegramLongPollingBot для поддержки long polling подключения к Telegram API.
 */
@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private final WhisperService whisperService;
    private final ExternalApiService externalApiService;

    /**
     * Конструктор бота, инициализирующий основные команды.
     *
     * @param botConfig конфигурация бота, содержащая токен и имя пользователя
     * @param userRepository репозиторий для работы с данными пользователей
     * @param whisperService сервис для работы с голосовыми сообщениями
     * @param externalApiService сервис для работы с внешним API
     */
    public TelegramBot(BotConfig botConfig, UserRepository userRepository,
                      WhisperService whisperService, ExternalApiService externalApiService) {
        this.botConfig = botConfig;
        this.userRepository = userRepository;
        this.whisperService = whisperService;
        this.externalApiService = externalApiService;
        
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Получить ваш ID чата"));
        
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при установке списка команд бота: " + e.getMessage());
        }
    }

    /**
     * Получает имя пользователя бота из конфигурации.
     *
     * @return имя пользователя бота
     */
    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    /**
     * Получает токен бота из конфигурации.
     *
     * @return токен бота для аутентификации в Telegram API
     */
    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    /**
     * Обрабатывает входящие обновления (сообщения) от пользователей.
     * Проверяет регистрацию пользователя и тип сообщения (текст/голос).
     *
     * @param update объект, содержащий информацию о входящем обновлении
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        
        if (!userRepository.existsByChatId(chatId)) {
            sendMessage(chatId, "Вы не зарегистрированы. Пожалуйста, зарегистрируйтесь через внешний сервис.");
            return;
        }

        if (update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            
            if (messageText.equals("/start")) {
                sendMessage(chatId, "Ваш ID чата: " + chatId);
                return;
            }
            
            // Process text message
            processTextMessage(chatId, messageText);
        } else if (update.getMessage().hasVoice()) {
            // Process voice message
            processVoiceMessage(chatId, update.getMessage().getVoice());
        }
    }

    /**
     * Обрабатывает текстовые сообщения от пользователей.
     *
     * @param chatId ID чата пользователя
     * @param text текст сообщения для обработки
     */
    private void processTextMessage(long chatId, String text) {
        try {
            String response = externalApiService.processText(text);
            sendMessage(chatId, response);
        } catch (Exception e) {
            log.error("Ошибка при обработке текстового сообщения: {}", e.getMessage());
            sendMessage(chatId, "Извините, произошла ошибка при обработке вашего сообщения");
        }
    }

    /**
     * Обрабатывает голосовые сообщения от пользователей.
     *
     * @param chatId ID чата пользователя
     * @param voice объект голосового сообщения
     */
    private void processVoiceMessage(long chatId, org.telegram.telegrambots.meta.api.objects.Voice voice) {
        try {
            // Преобразуем голосовое сообщение в текст
            String text = whisperService.transcribeVoice(voice);
            if (text != null && !text.isEmpty()) {
                // Отправляем пользователю распознанный текст
                sendMessage(chatId, "Распознанный текст: " + text);
                
                // Обрабатываем распознанный текст через внешнее API
                String response = externalApiService.processText(text);
                sendMessage(chatId, response);
            } else {
                sendMessage(chatId, "Извините, не удалось распознать голосовое сообщение");
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке голосового сообщения: {}", e.getMessage());
            sendMessage(chatId, "Извините, произошла ошибка при обработке вашего голосового сообщения");
        }
    }

    /**
     * Отправляет текстовое сообщение в указанный чат.
     *
     * @param chatId ID чата для отправки сообщения
     * @param text текст сообщения для отправки
     */
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}
