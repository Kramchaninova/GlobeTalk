package org.example.Interface;

import org.example.Authentication.AuthService;
import org.example.Authentication.AuthServiceImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * UserService с системой блокировки пользователей
 */
public class UserService {
    /**
     * Заблокированные пользователи - не получают рассылки
     * Блокируются когда: заняты тестом, не авторизованы, недоступен канал
     */
    private final Set<Long> blockedUsers = new HashSet<>();

    private final AuthService authService;

    private Set<Long> telegramUsersCache;
    private Set<Long> discordUsersCache;
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 30000; // 30 секунд

    public UserService() {
        this.authService = new AuthServiceImpl();
        updateCache();
    }

    /**
     * Обновляет кэш списков пользователей
     */
    private void updateCache() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_TTL) {
            this.telegramUsersCache = authService.getAllTelegramUsers();
            this.discordUsersCache = authService.getAllDiscordUsers();
            this.lastCacheUpdate = currentTime;
            System.out.println("[UserService] Кэш пользователей обновлен");
        }
    }

    /**
     * Получает ТОЛЬКО Telegram пользователей для рассылки
     */
    public Set<Long> getActiveTelegramUsers() {
        updateCache();
        Set<Long> activeUsers = new HashSet<>();

        for (Long userId : telegramUsersCache) {
            if (!isUserBlocked(userId)) activeUsers.add(userId);
        }

        System.out.println("[UserService] Активных Telegram: " + activeUsers.size());
        return activeUsers;
    }

    /**
     * Получает ТОЛЬКО Discord пользователей для рассылки
     */
    public Set<Long> getActiveDiscordUsers() {
        updateCache();
        Set<Long> activeUsers = new HashSet<>();

        for (Long userId : discordUsersCache) {
            if (!isUserBlocked(userId)) activeUsers.add(userId);
        }

        System.out.println("[UserService] Активных Discord: " + activeUsers.size());
        return activeUsers;
    }

    /**
     * Получает всех активных пользователей (для обратной совместимости)
     */
    public Set<Long> getActiveUsers() {
        Set<Long> activeUsers = new HashSet<>();
        activeUsers.addAll(getActiveTelegramUsers());
        activeUsers.addAll(getActiveDiscordUsers());
        return activeUsers;
    }

    /**
     * БЛОКИРУЕТ пользователя - добавляет в blockedUsers
     * Пользователь перестает получать рассылки
     */
    public void blockUser(long chatId) {
        blockedUsers.add(chatId);
        System.out.println("[UserService] * Заблокирован: " + chatId);
        logStatistics();
    }

    /**
     * РАЗБЛОКИРУЕТ пользователя - убирает из blockedUsers
     * Пользователь снова может получать рассылки
     */
    public void unblockUser(long chatId) {
        blockedUsers.remove(chatId);
        System.out.println("[UserService] Разблокирован: " + chatId);
        logStatistics();
    }

    /**
     * Проверяет, заблокирован ли пользователь для рассылок
     */
    public boolean isUserBlocked(long chatId) {
        return blockedUsers.contains(chatId);
    }

    /**
     * Определяет тип платформы по ID
     */
    public String getPlatformType(long userId) {
        updateCache();
        return discordUsersCache.contains(userId) ? "discord" :
                telegramUsersCache.contains(userId) ? "telegram" : "unknown";
    }

    /**
     * Обрабатывает ошибку отправки
     */
    public void handleSendError(long userId, Exception e) {
        String platform = getPlatformType(userId);
        String error = e.getMessage();

        System.out.println("[UserService] Анализ ошибки: " + userId + " -> " + platform);

        // Discord ошибки - не критичны, просто отвязываем
        if (error != null && (error.contains("не найден"))) {
            System.out.println("Discord канал недоступен, отвязываем: " + userId);
            authService.unlinkCurrentChat(userId);
            blockUser(userId); // Блокируем чтобы больше не пытаться отправлять
            updateCache();
        }
        // Telegram ошибки - логируем
        else if ("telegram".equals(platform)) {
            System.err.println("[UserService]"+"Ошибка Telegram: " + userId + " - " + error);
        }
        // Прочие ошибки
        else {
            System.out.println("[UserService]"+" Ошибка отправки: " + userId + " (" + platform + ") - " + error);
            authService.unlinkCurrentChat(userId);
            blockUser(userId); // Блокируем при любых других ошибках
            updateCache();
        }
    }

    public void addUser(long chatId) {
        System.out.println("[UserService] Обработан: " + chatId);
    }

    /**
     * Используйте blockUser()
     */
    public void freezeUser(long chatId) {
        blockUser(chatId);
    }

    /**
     * Используйте unblockUser()
     */
    public void unfreezeUser(long chatId) {
        unblockUser(chatId);
    }

    /**
     * Используйте isUserBlocked()
     */
    public boolean isUserBusy(long chatId) {
        return isUserBlocked(chatId);
    }

    /**
     * Используйте isUserBlocked()
     */
    public boolean isUserFrozen(long chatId) {
        return isUserBlocked(chatId);
    }

    public void updateUserActivity(long chatId) {
        addUser(chatId);
    }

    private void logStatistics() {
        updateCache();
        int total = telegramUsersCache.size() + discordUsersCache.size();
        System.out.println("[UserService] Статистика: " +
                "всего=" + total +
                " (TG:" + telegramUsersCache.size() + ",DC:" + discordUsersCache.size() + ")" +
                ", заблокировано=" + blockedUsers.size());
    }

    public void cleanupInactiveUsers() {
        System.out.println("[UserService] Очистка неактивных пользователей");
        updateCache();
    }

    public void unfreezeAllUsers() {
        blockedUsers.clear();
        System.out.println("[UserService] Все пользователи разблокированы");
        logStatistics();
    }
}