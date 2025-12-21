package org.example;

import org.example.Data.BotResponse;
import org.example.SheduleMessages.DistributionService;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Тестирование DistributionService с использованием Fake реализации
 */
public class DistributionServiceTest {

    /**
     * Fake реализация DistributionService для тестирования
     */
    private static class FakeDistributionService implements DistributionService {
        private final List<String> operations = new ArrayList<>();
        private final AtomicInteger startCallCount = new AtomicInteger(0);
        private final AtomicInteger stopCallCount = new AtomicInteger(0);
        private boolean isRunning = false;

        @Override
        public void startDistribution(int initialDelay, int period) {
            operations.add("startDistribution: initialDelay=" + initialDelay + ", period=" + period);
            startCallCount.incrementAndGet();
            isRunning = true;
        }

        @Override
        public void stopDistribution() {
            operations.add("stopDistribution");
            stopCallCount.incrementAndGet();
            isRunning = false;
        }

        /**
         * @return список выполненных операций
         */
        public List<String> getOperations() {
            return new ArrayList<>(operations);
        }

        /**
         * @return количество запусков распределения
         */
        public int getStartCallCount() {
            return startCallCount.get();
        }

        /**
         * @return количество остановок распределения
         */
        public int getStopCallCount() {
            return stopCallCount.get();
        }

        /**
         * @return true если распределение активно
         */
        public boolean isRunning() {
            return isRunning;
        }
    }

    /**
     * Упрощенный Fake BotLogic для тестирования
     */
    private static class SimpleFakeBotLogic {
        private final List<Long> generatedUsers = new ArrayList<>();
        private boolean shouldReturnNull = false;

        /**
         * Генерирует отложенное сообщение
         */
        public BotResponse generateScheduledMessage(long userId) {
            return generateResponse(userId, "Ежедневное слово");
        }

        /**
         * Генерирует отложенный тест
         */
        public BotResponse generateScheduledTest(long userId) {
            return generateResponse(userId, "Тест");
        }

        /**
         * Генерирует сообщение со старым словом
         */
        public BotResponse generateScheduledOldWord(long userId) {
            return generateResponse(userId, "Старое слово");
        }

        /**
         * Генерирует BotResponse для пользователя
         */
        private BotResponse generateResponse(long userId, String messageType) {
            generatedUsers.add(userId);
            if (shouldReturnNull) return null;
            return new BotResponse(userId, messageType + " для пользователя " + userId);
        }

        /**
         * Устанавливает флаг возврата null
         */
        public void setShouldReturnNull(boolean shouldReturnNull) {
            this.shouldReturnNull = shouldReturnNull;
        }

        /**
         * @return список пользователей для генерации сообщений
         */
        public List<Long> getGeneratedUsers() {
            return new ArrayList<>(generatedUsers);
        }
    }

    /**
     * Упрощенный Fake UserService для тестирования
     */
    private static class SimpleFakeUserService {
        private final List<Long> telegramUsers = new ArrayList<>();
        private final List<Long> discordUsers = new ArrayList<>();
        private final List<String> handledErrors = new ArrayList<>();

        /**
         * Добавляет Telegram пользователя
         */
        public void addTelegramUser(long userId) {
            telegramUsers.add(userId);
        }

        /**
         * Добавляет Discord пользователя
         */
        public void addDiscordUser(long userId) {
            discordUsers.add(userId);
        }

        /**
         * @return активных Telegram пользователей
         */
        public Set<Long> getActiveTelegramUsers() {
            return new HashSet<>(telegramUsers);
        }

        /**
         * @return активных Discord пользователей
         */
        public Set<Long> getActiveDiscordUsers() {
            return new HashSet<>(discordUsers);
        }

        /**
         * Обрабатывает ошибку отправки
         */
        public void handleSendError(long userId, Exception e) {
            handledErrors.add("Error for user " + userId + ": " + e.getMessage());
        }

        /**
         * @return список обработанных ошибок
         */
        public List<String> getHandledErrors() {
            return new ArrayList<>(handledErrors);
        }
    }

    /**
     * Fake MessageSender для тестирования отправки сообщений
     */
    private static class FakeMessageSender implements Function<BotResponse, Boolean> {
        private final List<BotResponse> sentMessages = new ArrayList<>();
        private boolean shouldFail = false;

        @Override
        public Boolean apply(BotResponse response) {
            sentMessages.add(response);
            return !shouldFail;
        }

        /**
         * @return список отправленных сообщений
         */
        public List<BotResponse> getSentMessages() {
            return new ArrayList<>(sentMessages);
        }

        /**
         * Устанавливает флаг неудачной отправки
         */
        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        /**
         * Очищает список сообщений
         */
        public void clear() {
            sentMessages.clear();
        }
    }




    //  ТЕСТЫ

    /**
     * Тест запуска распределения сообщений
     */
    @Test
    public void testStartDistribution() {
        FakeDistributionService fakeService = new FakeDistributionService();
        fakeService.startDistribution(10, 60);

        Assert.assertEquals(1, fakeService.getStartCallCount());
        Assert.assertTrue(fakeService.isRunning());
        Assert.assertEquals("startDistribution: initialDelay=10, period=60",
                fakeService.getOperations().get(0));
    }

    /**
     * Тест остановки распределения сообщений
     */
    @Test
    public void testStopDistribution() {
        FakeDistributionService fakeService = new FakeDistributionService();
        fakeService.startDistribution(5, 30);
        fakeService.stopDistribution();

        Assert.assertEquals(1, fakeService.getStopCallCount());
        Assert.assertFalse(fakeService.isRunning());
        Assert.assertEquals(2, fakeService.getOperations().size());
        Assert.assertEquals("stopDistribution", fakeService.getOperations().get(1));
    }

    /**
     * Тест множественных операций запуска/остановки
     */
    @Test
    public void testMultipleOperations() {
        FakeDistributionService fakeService = new FakeDistributionService();

        fakeService.startDistribution(10, 60);
        fakeService.startDistribution(5, 30);
        fakeService.stopDistribution();
        fakeService.startDistribution(1, 10);

        Assert.assertEquals(3, fakeService.getStartCallCount());
        Assert.assertEquals(1, fakeService.getStopCallCount());
        Assert.assertTrue(fakeService.isRunning());
        Assert.assertEquals(4, fakeService.getOperations().size());
    }

    /**
     * Тест Fake BotLogic
     */
    @Test
    public void testFakeBotLogic() {
        SimpleFakeBotLogic botLogic = new SimpleFakeBotLogic();

        BotResponse response1 = botLogic.generateScheduledMessage(123L);
        BotResponse response2 = botLogic.generateScheduledTest(456L);

        Assert.assertNotNull(response1);
        Assert.assertNotNull(response2);
        Assert.assertEquals(2, botLogic.getGeneratedUsers().size());
        Assert.assertTrue(botLogic.getGeneratedUsers().contains(123L));
        Assert.assertTrue(botLogic.getGeneratedUsers().contains(456L));
    }

    /**
     * Тест Fake UserService
     */
    @Test
    public void testFakeUserService() {
        SimpleFakeUserService userService = new SimpleFakeUserService();

        userService.addTelegramUser(123L);
        userService.addDiscordUser(456L);

        Assert.assertEquals(1, userService.getActiveTelegramUsers().size());
        Assert.assertEquals(1, userService.getActiveDiscordUsers().size());
        Assert.assertTrue(userService.getActiveTelegramUsers().contains(123L));
        Assert.assertTrue(userService.getActiveDiscordUsers().contains(456L));
    }

    /**
     * Тест Fake MessageSender
     */
    @Test
    public void testFakeMessageSender() {
        FakeMessageSender sender = new FakeMessageSender();
        BotResponse response = new BotResponse(123L, "Test message");

        boolean result = sender.apply(response);

        Assert.assertTrue(result);
        Assert.assertEquals(1, sender.getSentMessages().size());
        Assert.assertEquals(response, sender.getSentMessages().get(0));
    }

    /**
     * Тест валидации BotResponse
     */
    @Test
    public void testBotResponseValidation() {
        // Тест валидного ответа
        BotResponse validResponse = new BotResponse(123L, "Valid message");
        Assert.assertTrue(validResponse.isValid());

        // Тест невалидного ответа (пустой текст)
        BotResponse invalidResponse = new BotResponse(123L, "");
        Assert.assertFalse(invalidResponse.isValid());

        // Тест невалидного ответа (null текст)
        BotResponse invalidResponse2 = new BotResponse(123L, null);
        Assert.assertFalse(invalidResponse2.isValid());
    }

    /**
     * Тест BotLogic с возвратом null
     */
    @Test
    public void testBotLogicReturnsNull() {
        SimpleFakeBotLogic botLogic = new SimpleFakeBotLogic();
        botLogic.setShouldReturnNull(true);

        BotResponse response = botLogic.generateScheduledMessage(123L);

        Assert.assertNull(response);
        Assert.assertEquals(1, botLogic.getGeneratedUsers().size());
    }

    /**
     * Тест MessageSender с неудачной отправкой
     */
    @Test
    public void testMessageSenderFailure() {
        FakeMessageSender sender = new FakeMessageSender();
        sender.setShouldFail(true);

        BotResponse response = new BotResponse(123L, "Test message");
        boolean result = sender.apply(response);

        Assert.assertFalse(result);
        Assert.assertEquals(1, sender.getSentMessages().size());
    }

    /**
     * Тест обработки ошибок в UserService
     */
    @Test
    public void testUserServiceErrorHandling() {
        SimpleFakeUserService userService = new SimpleFakeUserService();

        userService.handleSendError(123L, new Exception("Send failed"));

        Assert.assertEquals(1, userService.getHandledErrors().size());
        Assert.assertTrue(userService.getHandledErrors().get(0).contains("Send failed"));
    }
    /**
     * Тест запуска распределений с разными задержками
     */
    @Test
    public void testDistributionWithDifferentDelays() {
        FakeDistributionService wordService = new FakeDistributionService();
        FakeDistributionService testService = new FakeDistributionService();
        FakeDistributionService oldWordService = new FakeDistributionService();

        // Имитируем запуск с таймерами как в TelegramBot
        wordService.startDistribution(100, 5 * 60);    // ежедневные слова
        testService.startDistribution(150, 3 * 60);    // отложенные тесты
        oldWordService.startDistribution(60, 60);      // старое слово

        // Проверяем что все сервисы запущены
        Assert.assertTrue(wordService.isRunning());
        Assert.assertTrue(testService.isRunning());
        Assert.assertTrue(oldWordService.isRunning());

        // Проверяем параметры таймеров
        Assert.assertEquals("startDistribution: initialDelay=100, period=300",
                wordService.getOperations().get(0));
        Assert.assertEquals("startDistribution: initialDelay=150, period=180",
                testService.getOperations().get(0));
        Assert.assertEquals("startDistribution: initialDelay=60, period=60",
                oldWordService.getOperations().get(0));
    }

    /**
     * Тест остановки всех распределений
     */
    @Test
    public void testStopAllDistributions() {
        FakeDistributionService wordService = new FakeDistributionService();
        FakeDistributionService testService = new FakeDistributionService();
        FakeDistributionService oldWordService = new FakeDistributionService();

        // Запускаем все распределения
        wordService.startDistribution(100, 300);
        testService.startDistribution(150, 180);
        oldWordService.startDistribution(60, 60);

        // Останавливаем все (имитация shutdown)
        wordService.stopDistribution();
        testService.stopDistribution();
        oldWordService.stopDistribution();

        // Проверяем что все остановлены
        Assert.assertFalse(wordService.isRunning());
        Assert.assertFalse(testService.isRunning());
        Assert.assertFalse(oldWordService.isRunning());

        // Проверяем количество операций
        Assert.assertEquals(2, wordService.getOperations().size());
        Assert.assertEquals(2, testService.getOperations().size());
        Assert.assertEquals(2, oldWordService.getOperations().size());
    }

    /**
     * Тест последовательного запуска распределений
     */
    @Test
    public void testSequentialDistributionStart() {
        FakeDistributionService service1 = new FakeDistributionService();
        FakeDistributionService service2 = new FakeDistributionService();
        FakeDistributionService service3 = new FakeDistributionService();

        // Последовательный запуск как в конструкторе TelegramBot
        service1.startDistribution(100, 300);
        service2.startDistribution(150, 180);
        service3.startDistribution(60, 60);

        // Проверяем что все запущены в правильном порядке
        Assert.assertTrue(service1.isRunning());
        Assert.assertTrue(service2.isRunning());
        Assert.assertTrue(service3.isRunning());

        // Проверяем параметры
        Assert.assertEquals(100, extractInitialDelay(service1.getOperations().get(0)));
        Assert.assertEquals(150, extractInitialDelay(service2.getOperations().get(0)));
        Assert.assertEquals(60, extractInitialDelay(service3.getOperations().get(0)));
    }

    /**
     * Вспомогательный метод для извлечения initialDelay из строки операции
     */
    private int extractInitialDelay(String operation) {
        // Парсим строку вида "startDistribution: initialDelay=100, period=300"
        String[] parts = operation.split("initialDelay=");
        if (parts.length > 1) {
            String delayPart = parts[1].split(",")[0];
            return Integer.parseInt(delayPart);
        }
        return -1;
    }

    /**
     * Тест повторного запуска после остановки
     */
    @Test
    public void testRestartDistribution() {
        FakeDistributionService service = new FakeDistributionService();

        // Первый запуск
        service.startDistribution(100, 300);
        Assert.assertTrue(service.isRunning());

        // Остановка
        service.stopDistribution();
        Assert.assertFalse(service.isRunning());

        // Повторный запуск с другими параметрами
        service.startDistribution(50, 200);
        Assert.assertTrue(service.isRunning());

        // Проверяем историю операций
        Assert.assertEquals(3, service.getOperations().size());
        Assert.assertEquals("startDistribution: initialDelay=100, period=300",
                service.getOperations().get(0));
        Assert.assertEquals("stopDistribution", service.getOperations().get(1));
        Assert.assertEquals("startDistribution: initialDelay=50, period=200",
                service.getOperations().get(2));
    }

    /**
     * Тест различных комбинаций задержек и периодов
     */
    @Test
    public void testVariousTimerCombinations() {
        testTimerCombination(0, 60);    // минимальные значения
        testTimerCombination(1000, 3600); // большие значения
        testTimerCombination(30, 120);   // средние значения
    }

    /**
     * Приватный метод для тестирования комбинацию параметров таймера распределения
     */
    private void testTimerCombination(int initialDelay, int period) {
        // 1. Создаем фейк-сервис
        FakeDistributionService service = new FakeDistributionService();

        // 2. Запускаем распределение с заданными параметрами
        service.startDistribution(initialDelay, period);

        // 3. Проверяем что сервис запустился
        Assert.assertTrue(service.isRunning());

        // 4. Проверяем что операция записалась с правильными параметрами
        String expectedOperation = String.format(
                "startDistribution: initialDelay=%d, period=%d", initialDelay, period);
        Assert.assertEquals(expectedOperation, service.getOperations().get(0));

        // 5. Останавливаем сервис (чистка)
        service.stopDistribution();
    }
}