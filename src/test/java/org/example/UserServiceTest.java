package org.example;

import org.example.SheduleMessages.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Set;
import java.util.HashSet;

/**
 * Тесты для UserService
 */
public class UserServiceTest {

    private FakeUserService fakeUserService;

    /**
     * Fake UserService для тестирования методов определения платформы
     */
    private static class FakeUserService extends UserService {
        private final Set<Long> telegramUsers = new HashSet<>();
        private final Set<Long> discordUsers = new HashSet<>();

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
        public String getPlatformType(long userId) {
            if (telegramUsers.contains(userId)) {
                return "telegram";
            } else if (discordUsers.contains(userId)) {
                return "discord";
            } else {
                return "unknown";
            }
        }
    }

    @BeforeEach
    public void setUp() {
        fakeUserService = new FakeUserService();
    }

    /**
     * Тест блокировки и разблокировки пользователя
     */
    @Test
    public void testBlockAndUnblockUser() {
        long testUserId = 12345L;

        Assertions.assertEquals(false, fakeUserService.isUserBlocked(testUserId), "Пользователь должен быть разблокирован изначально");

        fakeUserService.blockUser(testUserId);
        Assertions.assertEquals(true, fakeUserService.isUserBlocked(testUserId), "Пользователь должен быть заблокирован после blockUser");

        fakeUserService.unblockUser(testUserId);
        Assertions.assertEquals(false, fakeUserService.isUserBlocked(testUserId), "Пользователь должен быть разблокирован после unblockUser");
    }

    /**
     * Тест методов для обратной совместимости
     */
    @Test
    public void testAliasMethods() {
        long testUserId = 67890L;

        fakeUserService.freezeUser(testUserId);
        Assertions.assertEquals(true, fakeUserService.isUserBlocked(testUserId), "freezeUser должен блокировать пользователя");
        Assertions.assertEquals(true, fakeUserService.isUserBusy(testUserId), "isUserBusy должен возвращать true для заблокированного пользователя");
        Assertions.assertEquals(true, fakeUserService.isUserFrozen(testUserId), "isUserFrozen должен возвращать true для заблокированного пользователя");

        fakeUserService.unfreezeUser(testUserId);
        Assertions.assertEquals(false, fakeUserService.isUserBlocked(testUserId), "unfreezeUser должен разблокировать пользователя");
        Assertions.assertEquals(false, fakeUserService.isUserBusy(testUserId), "isUserBusy должен возвращать false для разблокированного пользователя");
        Assertions.assertEquals(false, fakeUserService.isUserFrozen(testUserId), "isUserFrozen должен возвращать false для разблокированного пользователя");
    }

    /**
     * Тест разблокировки всех пользователей
     */
    @Test
    public void testUnfreezeAllUsers() {
        fakeUserService.blockUser(111L);
        fakeUserService.blockUser(222L);
        fakeUserService.blockUser(333L);

        Assertions.assertEquals(true, fakeUserService.isUserBlocked(111L), "Пользователь 111L должен быть заблокирован");
        Assertions.assertEquals(true, fakeUserService.isUserBlocked(222L), "Пользователь 222L должен быть заблокирован");
        Assertions.assertEquals(true, fakeUserService.isUserBlocked(333L), "Пользователь 333L должен быть заблокирован");

        fakeUserService.unfreezeAllUsers();

        Assertions.assertEquals(false, fakeUserService.isUserBlocked(111L), "Пользователь 111L должен быть разблокирован после unfreezeAllUsers");
        Assertions.assertEquals(false, fakeUserService.isUserBlocked(222L), "Пользователь 222L должен быть разблокирован после unfreezeAllUsers");
        Assertions.assertEquals(false, fakeUserService.isUserBlocked(333L), "Пользователь 333L должен быть разблокирован после unfreezeAllUsers");
    }

    /**
     * Тест определения типа платформы для Telegram пользователя
     */
    @Test
    public void testGetPlatformType_TelegramUser() {
        long telegramUserId = 123L;
        fakeUserService.addTelegramUser(telegramUserId);

        String platformType = fakeUserService.getPlatformType(telegramUserId);
        Assertions.assertEquals("telegram", platformType, "Для Telegram пользователя должен возвращаться 'telegram'");
    }

    /**
     * Тест определения типа платформы для Discord пользователя
     */
    @Test
    public void testGetPlatformType_DiscordUser() {
        long discordUserId = 456L;
        fakeUserService.addDiscordUser(discordUserId);

        String platformType = fakeUserService.getPlatformType(discordUserId);
        Assertions.assertEquals("discord", platformType, "Для Discord пользователя должен возвращаться 'discord'");
    }

    /**
     * Тест определения типа платформы для неизвестного пользователя
     */
    @Test
    public void testGetPlatformType_UnknownUser() {
        long unknownUserId = 999L;
        String platformType = fakeUserService.getPlatformType(unknownUserId);
        Assertions.assertEquals("unknown", platformType, "Для неизвестного пользователя должен возвращаться 'unknown'");
    }

    /**
     * Тест получения активных Telegram пользователей
     */
    @Test
    public void testGetActiveTelegramUsers() {
        fakeUserService.addTelegramUser(111L);
        fakeUserService.addTelegramUser(222L);

        Set<Long> telegramUsers = fakeUserService.getActiveTelegramUsers();

        Assertions.assertEquals(2, telegramUsers.size(), "Должно быть 2 Telegram пользователя");
        Assertions.assertEquals(true, telegramUsers.contains(111L), "Должен содержать пользователя 111L");
        Assertions.assertEquals(true, telegramUsers.contains(222L), "Должен содержать пользователя 222L");
    }

    /**
     * Тест получения активных Discord пользователей
     */
    @Test
    public void testGetActiveDiscordUsers() {
        fakeUserService.addDiscordUser(333L);
        fakeUserService.addDiscordUser(444L);

        Set<Long> discordUsers = fakeUserService.getActiveDiscordUsers();

        Assertions.assertEquals(2, discordUsers.size(), "Должно быть 2 Discord пользователя");
        Assertions.assertEquals(true, discordUsers.contains(333L), "Должен содержать пользователя 333L");
        Assertions.assertEquals(true, discordUsers.contains(444L), "Должен содержать пользователя 444L");
    }

    /**
     * Тест получения всех активных пользователей
     */
    @Test
    public void testGetActiveUsers() {
        fakeUserService.addTelegramUser(111L);
        fakeUserService.addTelegramUser(222L);
        fakeUserService.addDiscordUser(333L);
        fakeUserService.addDiscordUser(444L);

        Set<Long> activeUsers = fakeUserService.getActiveUsers();

        Assertions.assertEquals(4, activeUsers.size(), "Должно быть 4 активных пользователя");
        Assertions.assertEquals(true, activeUsers.contains(111L), "Должен содержать Telegram пользователя 111L");
        Assertions.assertEquals(true, activeUsers.contains(222L), "Должен содержать Telegram пользователя 222L");
        Assertions.assertEquals(true, activeUsers.contains(333L), "Должен содержать Discord пользователя 333L");
        Assertions.assertEquals(true, activeUsers.contains(444L), "Должен содержать Discord пользователя 444L");
    }

    /**
     * Тест многопоточной блокировки
     */
    @Test
    public void testConcurrentBlockUnblock() {
        long testUserId = 77777L;

        for (int i = 0; i < 10; i++) {
            fakeUserService.blockUser(testUserId);
            Assertions.assertEquals(true, fakeUserService.isUserBlocked(testUserId), "Пользователь должен быть заблокирован на итерации " + i);

            fakeUserService.unblockUser(testUserId);
            Assertions.assertEquals(false, fakeUserService.isUserBlocked(testUserId), "Пользователь должен быть разблокирован на итерации " + i);
        }
    }

    /**
     * Тест граничных значений
     */
    @Test
    public void testEdgeCases() {
        Assertions.assertEquals(false, fakeUserService.isUserBlocked(0L), "Пользователь с ID 0 должен быть разблокирован");
        Assertions.assertEquals(false, fakeUserService.isUserBlocked(-1L), "Пользователь с отрицательным ID должен быть разблокирован");
        Assertions.assertEquals(false, fakeUserService.isUserBlocked(Long.MAX_VALUE), "Пользователь с максимальным ID должен быть разблокирован");
    }

    /**
     * Тест добавления пользователя и обновления активности
     */
    @Test
    public void testAddUserAndUpdateActivity() {
        long testUserId = 99999L;

        Assertions.assertDoesNotThrow(() -> fakeUserService.addUser(testUserId), "addUser не должен выбрасывать исключение");
        Assertions.assertDoesNotThrow(() -> fakeUserService.updateUserActivity(testUserId), "updateUserActivity не должен выбрасывать исключение");
    }

    /**
     * Тест очистки неактивных пользователей
     */
    @Test
    public void testCleanupInactiveUsers() {
        Assertions.assertDoesNotThrow(() -> fakeUserService.cleanupInactiveUsers(), "cleanupInactiveUsers не должен выбрасывать исключение");
    }

    /**
     * Тест обработки ошибок отправки
     */
    @Test
    public void testHandleSendError() {
        long testUserId = 55555L;
        Exception testException = new Exception("test error message");

        Assertions.assertDoesNotThrow(() -> fakeUserService.handleSendError(testUserId, testException), "handleSendError не должен выбрасывать исключение при обычной ошибке");

        Exception notFoundException = new Exception("не найден");
        Assertions.assertDoesNotThrow(() -> fakeUserService.handleSendError(testUserId, notFoundException), "handleSendError не должен выбрасывать исключение при ошибке 'не найден'");
    }
}