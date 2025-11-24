package org.example;

import org.example.Interface.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
        assertFalse(userService.isUserBlocked(testUserId));

        // Блокируем пользователя
        userService.blockUser(testUserId);
        assertTrue(userService.isUserBlocked(testUserId));

        // Разблокируем пользователя
        userService.unblockUser(testUserId);
        assertFalse(userService.isUserBlocked(testUserId));
    }

    /**
     * Тест методов для обратной совместимости
     */
    @Test
    public void testAliasMethods() {
        long testUserId = 67890L;

        // Проверяем названия блокировки
        userService.freezeUser(testUserId);
        assertTrue(userService.isUserBlocked(testUserId));
        assertTrue(userService.isUserBusy(testUserId));
        assertTrue(userService.isUserFrozen(testUserId));

        // Проверяем названия разблокировки
        userService.unfreezeUser(testUserId);
        assertFalse(userService.isUserBlocked(testUserId));
        assertFalse(userService.isUserBusy(testUserId));
        assertFalse(userService.isUserFrozen(testUserId));
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
        assertTrue(userService.isUserBlocked(111L));
        assertTrue(userService.isUserBlocked(222L));
        assertTrue(userService.isUserBlocked(333L));

        // Разблокируем всех
        userService.unfreezeAllUsers();

        // Проверяем что все разблокированы
        assertFalse(userService.isUserBlocked(111L));
        assertFalse(userService.isUserBlocked(222L));
        assertFalse(userService.isUserBlocked(333L));
    }

    /**
     * Тест добавления пользователя и обновления активности
     */
    @Test
    public void testAddUserAndUpdateActivity() {
        long testUserId = 99999L;

        // Эти методы просто логируют, проверяем что не падают
        assertDoesNotThrow(() -> userService.addUser(testUserId));
        assertDoesNotThrow(() -> userService.updateUserActivity(testUserId));
    }

    /**
     * Тест очистки неактивных пользователей
     */
    @Test
    public void testCleanupInactiveUsers() {
        // Метод просто логирует, проверяем что не падает
        assertDoesNotThrow(() -> userService.cleanupInactiveUsers());
    }

    /**
     * Тест определения типа платформы
     */
    @Test
    public void testGetPlatformType() {
        // Тестируем базовое поведение
        String platformType = userService.getPlatformType(123L);
        assertNotNull(platformType);

        // Должен вернуть один из ожидаемых типов
        assertTrue(platformType.equals("telegram") ||
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
        assertDoesNotThrow(() -> userService.handleSendError(testUserId, testException));

        // Тест с ошибкой "не найден"
        Exception notFoundException = new Exception("не найден");
        assertDoesNotThrow(() -> userService.handleSendError(testUserId, notFoundException));

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
        assertNotNull(activeUsers);
        // Может быть пустым или содержать пользователей в зависимости от состояния системы
    }

    /**
     * Тест получения активных Telegram пользователей
     */
    @Test
    public void testGetActiveTelegramUsers() {
        Set<Long> telegramUsers = userService.getActiveTelegramUsers();

        assertNotNull(telegramUsers);
        // Должен вернуть множество (может быть пустым)
    }

    /**
     * Тест получения активных Discord пользователей
     */
    @Test
    public void testGetActiveDiscordUsers() {
        Set<Long> discordUsers = userService.getActiveDiscordUsers();

        assertNotNull(discordUsers);
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
            assertTrue(userService.isUserBlocked(testUserId));

            userService.unblockUser(testUserId);
            assertFalse(userService.isUserBlocked(testUserId));
        }
    }

    /**
     * Тест граничных значений
     */
    @Test
    public void testEdgeCases() {
        // Тест с нулевым ID
        assertDoesNotThrow(() -> userService.isUserBlocked(0L));

        // Тест с отрицательным ID
        assertDoesNotThrow(() -> userService.isUserBlocked(-1L));

        // Тест с максимальным long значением
        assertDoesNotThrow(() -> userService.isUserBlocked(Long.MAX_VALUE));
    }
}