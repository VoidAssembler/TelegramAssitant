# Telegram Assistant Bot

Telegram бот-ассистент на Spring Boot с поддержкой обработки текстовых и голосовых сообщений.

## Возможности

- Обработка текстовых сообщений через внешний API
- Преобразование голосовых сообщений в текст с помощью Whisper API
- Хранение конфигурации в PostgreSQL
- Асинхронная загрузка конфигурации с автоматическими попытками переподключения
- Docker-ready с health checks

## Технологии

- Java 17
- Spring Boot 3.x
- PostgreSQL
- Docker & Docker Compose
- Telegram Bot API
- Whisper API для распознавания речи

## Требования

- Java 17+
- Docker и Docker Compose
- PostgreSQL (или Docker)
- Доступ к Telegram Bot API
- Доступ к Whisper API

## Установка и запуск

1. Клонируйте репозиторий:
```bash
git clone [repository-url]
cd telegram-assistant
```

2. Создайте файл .env на основе .env.example:
```bash
cp .env.example .env
```

3. Настройте переменные окружения в файле .env:
- Данные для подключения к PostgreSQL
- Настройки Spring Boot
- Уровни логирования

4. Запустите приложение через Docker Compose:
```bash
docker-compose up -d
```

## Конфигурация

### Настройка базы данных
При первом запуске автоматически создаются необходимые таблицы и базовые настройки.

### Настройка бота
1. Получите токен бота у @BotFather в Telegram
2. Добавьте токен и имя пользователя бота в базу данных через ConfigurationService

### Настройка внешних API
В базе данных необходимо указать:
- URL для Whisper API (whisper.api.url)
- URL для внешнего API обработки текста (external.api.url)

## Разработка

### Сборка проекта
```bash
./mvnw clean package
```

### Запуск тестов
```bash
./mvnw test
```

### Локальный запуск
```bash
./mvnw spring-boot:run
```

## CI/CD

Проект включает GitHub Actions workflow для:
- Сборки и тестирования
- Проверки стиля кода
- Сборки Docker образа
- Деплоя (требуется настройка)

## Структура проекта

```
src/
├── main/
│   ├── java/
│   │   └── com/telegram/assistant/
│   │       ├── config/         # Конфигурации Spring
│   │       ├── model/          # Модели данных
│   │       ├── repository/     # Репозитории
│   │       └── service/        # Бизнес-логика
│   └── resources/
│       └── application.yml     # Настройки приложения
```

## Лицензия

MIT License
