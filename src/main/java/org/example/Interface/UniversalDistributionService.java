package org.example.Interface;

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
        scheduler.scheduleAtFixedRate(this::distributeToUsers, initialDelay, period, TimeUnit.SECONDS);
        System.out.println("[Interface] " + distributionType + " для " + platform + " запущена");
    }

    /**
     * Останавливает рассылку и освобождает ресурсы
     */
    @Override
    public void stopDistribution() {
        scheduler.shutdown();
        System.out.println("[Interface] " + distributionType + " для " + platform + " остановлена");
    }

    /**
     * Основной метод рассылки - выполняет отправку сообщений пользователям текущей платформы
     * Автоматически фильтрует пользователей по платформе и обрабатывает ошибки отправки
     */
    private void distributeToUsers() {
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
                try {
                    BotResponse response = generateResponse(userId);

                    // РАЗДЕЛЯЕМ: null - это нормально (слово не найдено), не отправляем ошибку
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
                } catch (Exception e) {
                    handleError(userId, e);
                    errors++;
                    Thread.sleep(1000);
                }
            }

            System.out.println("[Interface] " + distributionType + " для " + platform +
                    " завершена. Успешно: " + success + ", Ошибок: " + errors + ", Пропущено: " + skipped);

        } catch (Exception e) {
            System.err.println("[Interface] Ошибка " + platform + " рассылки: " + e.getMessage());
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
     * @return BotResponse с сгенерированным контентом или null если генерация не удалась (нормальная ситуация)
     */
    private BotResponse generateResponse(long userId) {
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
            }
        } catch (Exception e) {
            System.err.println("[Interface] Ошибка генерации контента для пользователя " + userId + ": " + e.getMessage());
            return null;
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
                        errorMessage.contains("пользователь занят")
        )) {
            System.out.println("[Interface] Игнорируем временную ошибку для пользователя " + userId + ": " + errorMessage);
            return;
        }
        System.err.println("[Interface] Ошибка " + platform + " пользователю " + userId + ": " + errorMessage);

        //Передаем только реальные ошибки в UserService
        userService.handleSendError(userId, e);
    }
}