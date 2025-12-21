package org.example.SheduleMessages;

import org.example.BotLogic;
import org.example.Data.BotResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Универсальная реализация рассылки с автоматической фильтрацией по платформе
 * Обрабатывает рассылки для Telegram и Discord пользователей раздельно
 */
public class UniversalDistributionService implements DistributionService {
    private final BotLogic botLogic;
    private final Function<BotResponse, Boolean> messageSender;
    private final String distributionType;
    private final String platform; // "telegram" или "discord"
    private final ScheduledExecutorService scheduler;
    private final UserService userService;
    private volatile boolean isRunning = false;

    /**
     * Конструктор UniversalDistributionService
     *
     * @param botLogic логика бота для генерации контента
     * @param messageSender функция для отправки сообщений
     * @param distributionType тип рассылки ("ежедневные слова", "отложенные тесты", "старое слово")
     * @param platform платформа ("telegram" или "discord")
     */
    public UniversalDistributionService(BotLogic botLogic,
                                        Function<BotResponse, Boolean> messageSender,
                                        String distributionType,
                                        String platform) {
        this.botLogic = botLogic;
        this.messageSender = messageSender;
        this.distributionType = distributionType;
        this.platform = platform;
        this.userService = new UserService();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Запускает периодическую рассылку с указанными интервалами
     *
     * @param initialDelay начальная задержка перед первым запуском (в секундах)
     * @param period период между рассылками (в секундах)
     */
    @Override
    public void startDistribution(int initialDelay, int period) {
        if (isRunning) {
            System.out.println("[Interface] " + distributionType + " для " + platform + " уже запущена");
            return;
        }

        isRunning = true;
        scheduler.scheduleAtFixedRate(this::distributeToUsers, initialDelay, period, TimeUnit.SECONDS);
        System.out.println("[Interface] " + distributionType + " для " + platform + " запущена");
    }

    /**
     * Останавливает рассылку и освобождает ресурсы
     */
    @Override
    public void stopDistribution() {
        isRunning = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[Interface] " + distributionType + " для " + platform + " остановлена");
    }

    /**
     * Основной метод рассылки - выполняет отправку сообщений пользователям текущей платформы
     * Автоматически фильтрует пользователей по платформе и обрабатывает ошибки отправки
     */
    private void distributeToUsers() {
        if (!isRunning) {
            return;
        }

        try {
            System.out.println("[Interface] " + distributionType + ": запуск рассылки для " + platform);

            // Получаем пользователей ТОЛЬКО для этой платформы
            List<Long> users = getUsersForPlatform();

            if (users.isEmpty()) {
                System.out.println("[Interface] Нет " + platform + " пользователей для " + distributionType);
                return;
            }

            int success = 0, errors = 0, skipped = 0;

            for (Long userId : users) {
                if (!isRunning) {
                    System.out.println("[Interface] Рассылка прервана во время выполнения");
                    break;
                }

                try {
                    BotResponse response = generateResponse(userId);

                    // Обработка случаев, когда контент не сгенерирован
                    if (response == null) {
                        System.out.println("[Interface] " + distributionType + " пропущено для " + userId + " (контент не сгенерирован)");
                        skipped++;
                        continue;
                    }

                    if (!response.isValid()) {
                        System.out.println("[Interface] Невалидный ответ для пользователя " + userId + ", пропускаем");
                        skipped++;
                        continue;
                    }

                    // Попытка отправить сообщение
                    boolean sendResult = messageSender.apply(response);
                    if (sendResult) {
                        success++;
                        System.out.println("[Interface] " + distributionType + " отправлено: " + userId);
                    } else {
                        // Только если отправка не удалась - это ошибка
                        System.out.println("[Interface] Ошибка отправки для пользователя " + userId);
                        handleError(userId, new Exception("Ошибка отправки сообщения"));
                        errors++;
                    }
                } catch (ContentGenerationException e) {
                    // Специфичные ошибки генерации контента - пропускаем пользователя
                    System.out.println("[Interface] Ошибка генерации контента для " + userId + ": " + e.getMessage());
                    skipped++;
                } catch (ParseException e) {
                    // Ошибки парсинга - пропускаем пользователя
                    System.out.println("[Interface] Ошибка парсинга для " + userId + ": " + e.getMessage());
                    handleError(userId, e);
                    skipped++;
                } catch (Exception e) {
                    // Общие ошибки - логируем и продолжаем
                    System.out.println("[Interface] Неожиданная ошибка для пользователя " + userId + ": " + e.getMessage());
                    handleError(userId, e);
                    errors++;

                    // Небольшая пауза при ошибках чтобы не перегружать систему
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            System.out.println("[Interface] " + distributionType + " для " + platform +
                    " завершена. Успешно: " + success + ", Ошибок: " + errors + ", Пропущено: " + skipped);

        } catch (Exception e) {
            System.err.println("[Interface] Критическая ошибка " + platform + " рассылки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Получает список активных пользователей для текущей платформы
     *
     * @return список ID пользователей для текущей платформы
     */
    private List<Long> getUsersForPlatform() {
        Set<Long> users;
        if ("telegram".equals(platform)) {
            users = userService.getActiveTelegramUsers();
        } else if ("discord".equals(platform)) {
            users = userService.getActiveDiscordUsers();
        } else {
            users = userService.getActiveUsers(); // fallback
        }

        System.out.println("[Interface] Найдено " + users.size() + " активных пользователей для " + platform);
        return new java.util.ArrayList<>(users);
    }

    /**
     * Генерирует контент для рассылки на основе типа распределения
     *
     * @param userId ID пользователя для которого генерируется контент
     * @return BotResponse с сгенерированным контентом или null если генерация не удалась
     * @throws ContentGenerationException если произошла ошибка генерации контента
     * @throws ParseException если произошла ошибка парсинга ответа
     */
    private BotResponse generateResponse(long userId) throws ContentGenerationException, ParseException {
        System.out.println("[Interface] Генерация ответа для " + userId + ", тип рассылки: '" + distributionType + "'");

        BotResponse response = null;

        try {
            if ("ежедневные слова".equals(distributionType)) {
                response = botLogic.generateScheduledMessage(userId);
                System.out.println("[Interface] Вызван generateScheduledMessage() - ЕЖЕДНЕВНОЕ слово");
            } else if ("отложенные тесты".equals(distributionType)) {
                response = botLogic.generateScheduledTest(userId);
                System.out.println("[Interface] Вызван generateScheduledTest() - ТЕСТ");
            } else if ("старое слово".equals(distributionType)) {
                response = botLogic.generateScheduledOldWord(userId);
                System.out.println("[Interface] Вызван generateScheduledOldWord() - СТАРОЕ слово");
            } else {
                System.out.println("[Interface] Неизвестный тип рассылки: '" + distributionType + "'");
                return null;
            }
        } catch (RuntimeException e) {
            // Анализируем тип ошибки по сообщению
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("Не удалось распарсить") ||
                        errorMessage.contains("парсин") ||
                        errorMessage.contains("parse")) {
                    throw new ParseException("Ошибка парсинга сгенерированного контента для пользователя " + userId, e);
                } else if (errorMessage.contains("Openrouter") ||
                        errorMessage.contains("API") ||
                        errorMessage.contains("сеть") ||
                        errorMessage.contains("network")) {
                    throw new ContentGenerationException("Ошибка доступа к AI сервису для пользователя " + userId, e);
                }
            }
            // Если не удалось классифицировать - пробрасываем как общую ошибку генерации
            throw new ContentGenerationException("Ошибка генерации контента для пользователя " + userId, e);
        }

        if (response == null) {
            System.out.println("[Interface] Контент не сгенерирован для пользователя " + userId +
                    " (пользователь занят/нет слов/другая причина)");
        } else {
            System.out.println("[Interface] Контент успешно сгенерирован для пользователя " + userId);
        }

        return response;
    }

    /**
     * Обрабатывает ошибки отправки сообщений
     * Различает временные ошибки и критические ошибки канала
     *
     * @param userId ID пользователя у которого произошла ошибка
     * @param e исключение содержащее информацию об ошибке
     */
    private void handleError(long userId, Exception e) {
        String errorMessage = e.getMessage();

        // ИГНОРИРУЕМ временные ошибки генерации контента
        if (errorMessage != null && (
                errorMessage.contains("Ошибка генерации/отправки") ||
                        errorMessage.contains("контент не сгенерирован") ||
                        errorMessage.contains("слово не найдено") ||
                        errorMessage.contains("пользователь занят") ||
                        errorMessage.contains("Не удалось распарсить")
        )) {
            System.out.println("[Interface] Игнорируем временную ошибку для пользователя " + userId + ": " + errorMessage);
            return;
        }

        System.err.println("[Interface] Ошибка " + platform + " пользователю " + userId + ": " + errorMessage);

        // Передаем только реальные ошибки в UserService
        userService.handleSendError(userId, e);
    }

    /**
     * Проверяет, запущена ли рассылка
     * @return true если рассылка активна
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Исключение для ошибок генерации контента
     */
    private static class ContentGenerationException extends Exception {
        public ContentGenerationException(String message) {
            super(message);
        }

        public ContentGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Исключение для ошибок парсинга
     */
    private static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}