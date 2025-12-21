package org.example.Authentication;

import java.util.Set;

/**
 * AuthService - интерфейс для работы с аутентификацией пользователей.
 * Определяет контракт для операций с пользователями и их данными.
 */
public interface AuthService {

    /**
     * Регистрация нового пользователя
     */
    boolean registerUser(String username, String password);

    /**
     * Аутентификация пользователя
     */
    boolean authenticate(String username, String password);

    /**
     * Сброс пароля пользователя
     */
    boolean resetPassword(String username, String newPassword);

    /**
     * Изменение логина пользователя
     */
    boolean changeUsername(String oldUsername, String newUsername);

    /**
     * Получение оригинального логина пользователя
     */
    String getOriginalUsername(String currentUsername);

    /**
     * Привязка Telegram chat ID к учетной записи
     */
    boolean linkTelegramChat(String username, long telegramChatId);

    /**
     * Привязка Discord channel ID к учетной записи
     */
    boolean linkDiscordChannel(String username, long discordChannelId);

    /**
     * Проверка авторизации пользователя по Telegram chat ID
     */
    boolean isTelegramUserAuthorized(long telegramChatId);

    /**
     * Проверка авторизации пользователя по Discord channel ID
     */
    boolean isDiscordUserAuthorized(long discordChannelId);

    /**
     * Получение логина пользователя по Telegram chat ID
     */
    String getUsernameByTelegramChatId(long telegramChatId);

    /**
     * Получение логина пользователя по Discord channel ID
     */
    String getUsernameByDiscordChannelId(long discordChannelId);

    /**
     * Отвязка текущего чата от пользователя при выходе
     */
    boolean unlinkCurrentChat(long chatId);

    /**
     * Получает всех Telegram пользователей
     * @return множество Telegram chat_id всех авторизованных пользователей
     */
    Set<Long> getAllTelegramUsers();

    /**
     * Получает всех Discord пользователей
     * @return множество Discord channel_id всех авторизованных пользователей
     */
    Set<Long> getAllDiscordUsers();

}