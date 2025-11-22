package org.example.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * UserService - сервис для управления активными пользователями
 */
public class UserService {
    private final Set<Long> allUsers; // Все пользователи, которые когда-либо писали боту
    private final Set<Long> activeUsers; // Пользователи, которые могут получать рассылку
    private final Map<Long, Long> lastActivity; // Время последней активности пользователя

    public UserService() {
        this.allUsers = new CopyOnWriteArraySet<>();
        this.activeUsers = new CopyOnWriteArraySet<>();
        this.lastActivity = new ConcurrentHashMap<>();
    }

    /**
     * Добавляет пользователя при первом взаимодействии
     */
    public void addUser(long chatId) {
        allUsers.add(chatId);
        lastActivity.put(chatId, System.currentTimeMillis());
        System.out.println("Добавлен пользователь: " + chatId);
    }

    /**
     * Обновляет активность пользователя
     */
    public void updateUserActivity(long chatId) {
        lastActivity.put(chatId, System.currentTimeMillis());
        // Автоматически добавляем в активные, если пользователь авторизован и не занят
        activeUsers.add(chatId);
    }

    /**
     * Добавляет пользователя в рассылку
     */
    public void addToDistribution(long chatId) {
        activeUsers.add(chatId);
        System.out.println("Пользователь " + chatId + " добавлен в рассылку");
    }

    /**
     * Удаляет пользователя из рассылки
     */
    public void removeFromDistribution(long chatId) {
        activeUsers.remove(chatId);
        System.out.println("Пользователь " + chatId + " удален из рассылки");
    }

    /**
     * Получает всех активных пользователей для рассылки
     */
    public Set<Long> getActiveUsers() {
        return new HashSet<>(activeUsers);
    }

    /**
     * Получает всех пользователей бота
     */
    public Set<Long> getAllUsers() {
        return new HashSet<>(allUsers);
    }

    /**
     * Очищает неактивных пользователей (старше 30 дней)
     */
    public void cleanupInactiveUsers() {
        long thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        int removedCount = 0;

        for (Map.Entry<Long, Long> entry : lastActivity.entrySet()) {
            if (entry.getValue() < thirtyDaysAgo) {
                Long chatId = entry.getKey();
                allUsers.remove(chatId);
                activeUsers.remove(chatId);
                lastActivity.remove(chatId);
                removedCount++;
            }
        }

        if (removedCount > 0) {
            System.out.println("Очищено " + removedCount + " неактивных пользователей");
        }
    }


}