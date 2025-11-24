package org.example;

import org.example.Interface.DistributionService;
import org.example.Interface.UniversalDistributionService;
import org.example.Interface.UserService;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Юнит-тестирование DistributionService с использованием Fake реализации
 */
public class DistributionServiceTest {

    // Fake реализация для тестирования интерфейса DistributionService
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

        public List<String> getOperations() {
            return new ArrayList<>(operations);
        }

        public int getStartCallCount() {
            return startCallCount.get();
        }

        public int getStopCallCount() {
            return stopCallCount.get();
        }

        public boolean isRunning() {
            return isRunning;
        }
    }

    // Fake BotLogic который создает правильные BotResponse
    private static class FakeBotLogic extends org.example.BotLogic {
        private final List<Long> generatedUsers = new ArrayList<>();
        private boolean shouldReturnNull = false;
        private boolean shouldReturnInvalid = false;

        @Override
        public org.example.Data.BotResponse generateScheduledMessage(long userId) {
            generatedUsers.add(userId);
            if (shouldReturnNull) return null;
            return createBotResponse(userId, "Ежедневное слово");
        }

        @Override
        public org.example.Data.BotResponse generateScheduledTest(long userId) {
            generatedUsers.add(userId);
            if (shouldReturnNull) return null;
            return createBotResponse(userId, "Тест");
        }

        @Override
        public org.example.Data.BotResponse generateScheduledOldWord(long userId) {
            generatedUsers.add(userId);
            if (shouldReturnNull) return null;
            return createBotResponse(userId, "Старое слово");
        }

        private org.example.Data.BotResponse createBotResponse(long userId, String messageType) {
            if (shouldReturnInvalid) {
                // Создаем невалидный ответ (пустой текст)
                return new org.example.Data.BotResponse(userId, "");
            }
            return new org.example.Data.BotResponse(userId, messageType + " для пользователя " + userId);
        }

        public void setShouldReturnNull(boolean shouldReturnNull) {
            this.shouldReturnNull = shouldReturnNull;
        }

        public void setShouldReturnInvalid(boolean shouldReturnInvalid) {
            this.shouldReturnInvalid = shouldReturnInvalid;
        }

        public List<Long> getGeneratedUsers() {
            return new ArrayList<>(generatedUsers);
        }
    }

    // Fake UserService
    private static class FakeUserService extends UserService {
        private final List<Long> telegramUsers = new ArrayList<>();
        private final List<Long> discordUsers = new ArrayList<>();
        private final List<String> handledErrors = new ArrayList<>();

        public void addTelegramUser(long userId) {
            telegramUsers.add(userId);
        }

        public void addDiscordUser(long userId) {
            discordUsers.add(userId);
        }

        @Override
        public Set<Long> getActiveTelegramUsers() {
            return new HashSet<>(telegramUsers);
        }

        @Override
        public Set<Long> getActiveDiscordUsers() {
            return new HashSet<>(discordUsers);
        }

        @Override
        public void handleSendError(long userId, Exception e) {
            handledErrors.add("Error for user " + userId + ": " + e.getMessage());
        }

        public List<String> getHandledErrors() {
            return new ArrayList<>(handledErrors);
        }
    }

    // Fake MessageSender
    private static class FakeMessageSender implements Function<org.example.Data.BotResponse, Boolean> {
        private final List<org.example.Data.BotResponse> sentMessages = new ArrayList<>();
        private boolean shouldFail = false;

        @Override
        public Boolean apply(org.example.Data.BotResponse response) {
            sentMessages.add(response);
            return !shouldFail;
        }

        public List<org.example.Data.BotResponse> getSentMessages() {
            return new ArrayList<>(sentMessages);
        }

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        public void clear() {
            sentMessages.clear();
        }
    }

    // Testable UniversalDistributionService с рефлексией
    private static class TestableUniversalDistributionService {
        private final UniversalDistributionService realService;
        private final FakeUserService fakeUserService;

        public TestableUniversalDistributionService(org.example.BotLogic botLogic,
                                                    FakeMessageSender messageSender,
                                                    String distributionType,
                                                    String platform,
                                                    FakeUserService userService) {
            this.fakeUserService = userService;

            this.realService = new UniversalDistributionService(
                    botLogic,
                    messageSender,
                    distributionType,
                    platform
            );

            // Подменяем UserService через рефлексию
            try {
                var field = UniversalDistributionService.class.getDeclaredField("userService");
                field.setAccessible(true);
                field.set(realService, userService);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject fake UserService", e);
            }
        }

        public void startDistribution(int initialDelay, int period) {
            realService.startDistribution(initialDelay, period);
        }

        public void stopDistribution() {
            realService.stopDistribution();
        }
    }

    // ===== ТЕСТЫ =====

    @Test
    public void testStartDistribution() {
        FakeDistributionService fakeService = new FakeDistributionService();

        fakeService.startDistribution(10, 60);

        Assert.assertEquals(1, fakeService.getStartCallCount());
        Assert.assertTrue(fakeService.isRunning());
        Assert.assertEquals("startDistribution: initialDelay=10, period=60",
                fakeService.getOperations().get(0));
    }

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

    @Test
    public void testUniversalDistributionServiceWithNoUsers() throws InterruptedException {
        FakeBotLogic botLogic = new FakeBotLogic();
        FakeUserService fakeUserService = new FakeUserService();
        FakeMessageSender messageSender = new FakeMessageSender();

        TestableUniversalDistributionService service = new TestableUniversalDistributionService(
                botLogic, messageSender, "ежедневные слова", "telegram", fakeUserService);

        service.startDistribution(0, 1);
        Thread.sleep(100);

        Assert.assertEquals(0, messageSender.getSentMessages().size());
        Assert.assertEquals(0, botLogic.getGeneratedUsers().size());

        service.stopDistribution();
    }

    @Test
    public void testUniversalDistributionServiceWithTelegramUsers() throws InterruptedException {
        FakeBotLogic botLogic = new FakeBotLogic();
        FakeUserService fakeUserService = new FakeUserService();
        fakeUserService.addTelegramUser(123L);
        fakeUserService.addTelegramUser(456L);

        FakeMessageSender messageSender = new FakeMessageSender();

        TestableUniversalDistributionService service = new TestableUniversalDistributionService(
                botLogic, messageSender, "ежедневные слова", "telegram", fakeUserService);

        service.startDistribution(0, 1);
        Thread.sleep(100);

        Assert.assertEquals(2, botLogic.getGeneratedUsers().size());
        Assert.assertTrue(botLogic.getGeneratedUsers().contains(123L));
        Assert.assertTrue(botLogic.getGeneratedUsers().contains(456L));
        Assert.assertTrue("Should send messages to users", messageSender.getSentMessages().size() > 0);

        service.stopDistribution();
    }

    @Test
    public void testUniversalDistributionServiceWhenBotLogicReturnsNull() throws InterruptedException {
        FakeBotLogic botLogic = new FakeBotLogic();
        botLogic.setShouldReturnNull(true);

        FakeUserService fakeUserService = new FakeUserService();
        fakeUserService.addTelegramUser(123L);

        FakeMessageSender messageSender = new FakeMessageSender();

        TestableUniversalDistributionService service = new TestableUniversalDistributionService(
                botLogic, messageSender, "ежедневные слова", "telegram", fakeUserService);

        service.startDistribution(0, 1);
        Thread.sleep(100);

        Assert.assertEquals(1, botLogic.getGeneratedUsers().size());
        // Когда BotLogic возвращает null, сообщения не отправляются
        Assert.assertEquals(0, messageSender.getSentMessages().size());

        service.stopDistribution();
    }

    @Test
    public void testUniversalDistributionServiceWithInvalidBotResponse() throws InterruptedException {
        FakeBotLogic botLogic = new FakeBotLogic();
        botLogic.setShouldReturnInvalid(true); // BotLogic возвращает невалидные ответы

        FakeUserService fakeUserService = new FakeUserService();
        fakeUserService.addTelegramUser(123L);

        FakeMessageSender messageSender = new FakeMessageSender();

        TestableUniversalDistributionService service = new TestableUniversalDistributionService(
                botLogic, messageSender, "ежедневные слова", "telegram", fakeUserService);

        service.startDistribution(0, 1);
        Thread.sleep(100);

        Assert.assertEquals(1, botLogic.getGeneratedUsers().size());
        // Невалидные ответы не должны отправляться
        Assert.assertEquals(0, messageSender.getSentMessages().size());

        service.stopDistribution();
    }

    @Test
    public void testUniversalDistributionServiceWithFailedMessageSend() throws InterruptedException {
        FakeBotLogic botLogic = new FakeBotLogic();
        FakeUserService fakeUserService = new FakeUserService();
        fakeUserService.addTelegramUser(123L);

        FakeMessageSender messageSender = new FakeMessageSender();
        messageSender.setShouldFail(true);

        TestableUniversalDistributionService service = new TestableUniversalDistributionService(
                botLogic, messageSender, "ежедневные слова", "telegram", fakeUserService);

        service.startDistribution(0, 1);
        Thread.sleep(100);

        Assert.assertEquals(1, botLogic.getGeneratedUsers().size());
        Assert.assertTrue("Should attempt to send messages", messageSender.getSentMessages().size() > 0);
        Assert.assertTrue("Should handle send errors", fakeUserService.getHandledErrors().size() > 0);

        service.stopDistribution();
    }

    @Test
    public void testDifferentDistributionTypes() throws InterruptedException {
        testDistributionType("ежедневные слова");
        testDistributionType("отложенные тесты");
        testDistributionType("старое слово");
    }

    private void testDistributionType(String distributionType) throws InterruptedException {
        FakeBotLogic botLogic = new FakeBotLogic();
        FakeUserService fakeUserService = new FakeUserService();
        fakeUserService.addTelegramUser(123L);

        FakeMessageSender messageSender = new FakeMessageSender();

        TestableUniversalDistributionService service = new TestableUniversalDistributionService(
                botLogic, messageSender, distributionType, "telegram", fakeUserService);

        service.startDistribution(0, 1);
        Thread.sleep(100);

        Assert.assertTrue("Should work for distribution type: " + distributionType,
                botLogic.getGeneratedUsers().size() > 0);

        service.stopDistribution();
    }

    @Test
    public void testDiscordPlatform() throws InterruptedException {
        FakeBotLogic botLogic = new FakeBotLogic();
        FakeUserService fakeUserService = new FakeUserService();
        fakeUserService.addDiscordUser(789L);

        FakeMessageSender messageSender = new FakeMessageSender();

        TestableUniversalDistributionService service = new TestableUniversalDistributionService(
                botLogic, messageSender, "ежедневные слова", "discord", fakeUserService);

        service.startDistribution(0, 1);
        Thread.sleep(100);

        Assert.assertTrue("Should work for discord platform",
                botLogic.getGeneratedUsers().contains(789L));

        service.stopDistribution();
    }

    @Test
    public void testFakeUserService() {
        FakeUserService userService = new FakeUserService();

        userService.addTelegramUser(123L);
        userService.addDiscordUser(456L);

        Assert.assertEquals(1, userService.getActiveTelegramUsers().size());
        Assert.assertEquals(1, userService.getActiveDiscordUsers().size());
        Assert.assertTrue(userService.getActiveTelegramUsers().contains(123L));
        Assert.assertTrue(userService.getActiveDiscordUsers().contains(456L));
    }

    @Test
    public void testFakeMessageSender() {
        FakeMessageSender sender = new FakeMessageSender();
        org.example.Data.BotResponse response = new org.example.Data.BotResponse(123L, "Test message");

        boolean result = sender.apply(response);

        Assert.assertTrue(result);
        Assert.assertEquals(1, sender.getSentMessages().size());
        Assert.assertEquals(response, sender.getSentMessages().get(0));
    }

    @Test
    public void testFakeBotLogic() {
        FakeBotLogic botLogic = new FakeBotLogic();

        org.example.Data.BotResponse response1 = botLogic.generateScheduledMessage(123L);
        org.example.Data.BotResponse response2 = botLogic.generateScheduledTest(456L);

        Assert.assertNotNull(response1);
        Assert.assertNotNull(response2);
        Assert.assertTrue(response1.isValid());
        Assert.assertTrue(response2.isValid());
        Assert.assertEquals(2, botLogic.getGeneratedUsers().size());
        Assert.assertTrue(botLogic.getGeneratedUsers().contains(123L));
        Assert.assertTrue(botLogic.getGeneratedUsers().contains(456L));
    }

    @Test
    public void testBotResponseValidation() {
        // Test valid response
        org.example.Data.BotResponse validResponse = new org.example.Data.BotResponse(123L, "Valid message");
        Assert.assertTrue(validResponse.isValid());

        // Test invalid response (empty text)
        org.example.Data.BotResponse invalidResponse = new org.example.Data.BotResponse(123L, "");
        Assert.assertFalse(invalidResponse.isValid());

        // Test invalid response (null text)
        org.example.Data.BotResponse invalidResponse2 = new org.example.Data.BotResponse(123L, null);
        Assert.assertFalse(invalidResponse2.isValid());
    }
}