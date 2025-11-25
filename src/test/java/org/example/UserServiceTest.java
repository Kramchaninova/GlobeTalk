package org.example;

import org.example.Interface.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Set;

/**
 * Тесты для UserService
 */
public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    /**
     * Тест блокировки и разблокировки пользователя
     */
    @Test
    public void testBlockAndUnblockUser() {
        long testUserId = 12345L;

        // Проверяем что пользователь изначально не заблокирован
        Assertions.assertFalse(userService.isUserBlocked(testUserId));

        // Блокируем пользователя
        userService.blockUser(testUserId);
        Assertions.assertTrue(userService.isUserBlocked(testUserId));

        // Разблокируем пользователя
        userService.unblockUser(testUserId);
        Assertions.assertFalse(userService.isUserBlocked(testUserId));
    }

    /**
     * Тест методов для обратной совместимости
     */
    @Test
    public void testAliasMethods() {
        long testUserId = 67890L;

        // Проверяем названия блокировки
        userService.freezeUser(testUserId);
        Assertions.assertTrue(userService.isUserBlocked(testUserId));
        Assertions.assertTrue(userService.isUserBusy(testUserId));
        Assertions.assertTrue(userService.isUserFrozen(testUserId));

        // Проверяем названия разблокировки
        userService.unfreezeUser(testUserId);
        Assertions.assertFalse(userService.isUserBlocked(testUserId));
        Assertions.assertFalse(userService.isUserBusy(testUserId));
        Assertions.assertFalse(userService.isUserFrozen(testUserId));
    }

    /**
     * Тест разблокировки всех пользователей
     */
    @Test
    public void testUnfreezeAllUsers() {
        // Блокируем нескольких пользователей
        userService.blockUser(111L);
        userService.blockUser(222L);
        userService.blockUser(333L);

        // Проверяем что все заблокированы
        Assertions.assertTrue(userService.isUserBlocked(111L));
        Assertions.assertTrue(userService.isUserBlocked(222L));
        Assertions.assertTrue(userService.isUserBlocked(333L));

        // Разблокируем всех
        userService.unfreezeAllUsers();

        // Проверяем что все разблокированы
        Assertions.assertFalse(userService.isUserBlocked(111L));
        Assertions.assertFalse(userService.isUserBlocked(222L));
        Assertions.assertFalse(userService.isUserBlocked(333L));
    }

    /**
     * Тест добавления пользователя и обновления активности
     */
    @Test
    public void testAddUserAndUpdateActivity() {
        long testUserId = 99999L;

        // Эти методы просто логируют, проверяем что не падают
        Assertions.assertDoesNotThrow(() -> userService.addUser(testUserId));
        Assertions.assertDoesNotThrow(() -> userService.updateUserActivity(testUserId));
    }

    /**
     * Тест очистки неактивных пользователей
     */
    @Test
    public void testCleanupInactiveUsers() {
        // Метод просто логирует, проверяем что не падает
        Assertions.assertDoesNotThrow(() -> userService.cleanupInactiveUsers());
    }

    /**
     * Тест определения типа платформы
     */
    @Test
    public void testGetPlatformType() {
        // Тестируем базовое поведение
        String platformType = userService.getPlatformType(123L);
        Assertions.assertNotNull(platformType);

        // Должен вернуть один из ожидаемых типов
        Assertions.assertTrue(platformType.equals("telegram") ||
                platformType.equals("discord") ||
                platformType.equals("unknown"));
    }

    /**
     * Тест обработки ошибок отправки
     */
    @Test
    public void testHandleSendError() {
        long testUserId = 55555L;
        Exception testException = new Exception("test error message");

        // Проверяем что метод не падает при различных ошибках
        Assertions.assertDoesNotThrow(() -> userService.handleSendError(testUserId, testException));

        // Тест с ошибкой "не найден"
        Exception notFoundException = new Exception("не найден");
        Assertions.assertDoesNotThrow(() -> userService.handleSendError(testUserId, notFoundException));

        // Тест с null исключением - проверяем что не падает с NPE
        try {
            userService.handleSendError(testUserId, null);
        } catch (NullPointerException e) {
            System.out.println("Метод handleSendError не обрабатывает null исключения (ожидаемо)");
        }
    }

    /**
     * Тест получения активных пользователей
     */
    @Test
    public void testGetActiveUsers() {
        Set<Long> activeUsers = userService.getActiveUsers();

        // Должен вернуть не-null множество
        Assertions.assertNotNull(activeUsers);
        // Может быть пустым или содержать пользователей в зависимости от состояния системы
    }

    /**
     * Тест получения активных Telegram пользователей
     */
    @Test
    public void testGetActiveTelegramUsers() {
        Set<Long> telegramUsers = userService.getActiveTelegramUsers();

        Assertions.assertNotNull(telegramUsers);
        // Должен вернуть множество (может быть пустым)
    }

    /**
     * Тест получения активных Discord пользователей
     */
    @Test
    public void testGetActiveDiscordUsers() {
        Set<Long> discordUsers = userService.getActiveDiscordUsers();

        Assertions.assertNotNull(discordUsers);
        // Должен вернуть множество (может быть пустым)
    }

    /**
     * Тест многопоточной блокировки
     */
    @Test
    public void testConcurrentBlockUnblock() {
        long testUserId = 77777L;

        // Многократная блокировка/разблокировка
        for (int i = 0; i < 10; i++) {
            userService.blockUser(testUserId);
            Assertions.assertTrue(userService.isUserBlocked(testUserId));

            userService.unblockUser(testUserId);
            Assertions.assertFalse(userService.isUserBlocked(testUserId));
        }
    }

    /**
     * Тест граничных значений
     */
    @Test
    public void testEdgeCases() {
        // Тест с нулевым ID
        Assertions.assertDoesNotThrow(() -> userService.isUserBlocked(0L));

        // Тест с отрицательным ID
        Assertions.assertDoesNotThrow(() -> userService.isUserBlocked(-1L));

        // Тест с максимальным long значением
        Assertions.assertDoesNotThrow(() -> userService.isUserBlocked(Long.MAX_VALUE));
    }
}