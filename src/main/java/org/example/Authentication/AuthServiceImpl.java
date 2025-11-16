package org.example.Authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

/**
 * AuthServiceImpl - реализация работы с аутентификацией в SQLite.
 * Выполняет операции с базой данных пользователей.
 */
public class AuthServiceImpl implements AuthService {
    private Connection connection;

    /**
     * Конструктор - инициализирует подключение к БД
     */
    public AuthServiceImpl() {
        initializeDatabase();
    }

    /**
     * Инициализирует БД и создает таблицы если не существуют
     */
    private void initializeDatabase() {
        try {
            System.out.println("[Data Base] Инициализация базы данных аутентификации");
            connection = DriverManager.getConnection("jdbc:sqlite:bot_auth.db");

            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    original_username TEXT UNIQUE NOT NULL,
                    current_username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    telegram_chat_id INTEGER,
                    discord_channel_id INTEGER,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("[Data Base] Таблица users создана");
            }

        } catch (SQLException e) {
            System.err.println("[Data Base] Ошибка: " + e.getMessage());
            throw new RuntimeException("Ошибка инициализации базы данных", e);
        }
    }

    /**
     * Отвязывает текущий чат от пользователя
     */
    @Override
    public boolean unlinkCurrentChat(long chatId) {
        System.out.println("[Auth] Отвязка текущего чата: " + chatId);

        String telegramSQL = "UPDATE users SET telegram_chat_id = NULL WHERE telegram_chat_id = ?";
        String discordSQL = "UPDATE users SET discord_channel_id = NULL WHERE discord_channel_id = ?";

        try {
            boolean telegramUnlinked = false;
            boolean discordUnlinked = false;

            // Отвязываем Telegram чат
            try (PreparedStatement stmt = connection.prepareStatement(telegramSQL)) {
                stmt.setLong(1, chatId);
                int telegramRows = stmt.executeUpdate();
                telegramUnlinked = telegramRows > 0;
                if (telegramUnlinked) {
                    System.out.println("[Auth] Отвязан Telegram чат: " + chatId);
                }
            }

            // Отвязываем Discord канал
            try (PreparedStatement stmt = connection.prepareStatement(discordSQL)) {
                stmt.setLong(1, chatId);
                int discordRows = stmt.executeUpdate();
                discordUnlinked = discordRows > 0;
                if (discordUnlinked) {
                    System.out.println("[Auth] Отвязан Discord канал: " + chatId);
                }
            }

            boolean result = telegramUnlinked || discordUnlinked;
            System.out.println("[Auth] Текущий чат отвязан: " + result);
            return result;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка отвязки чата: " + e.getMessage());
            return false;
        }
    }

    /**
     * Регистрирует нового пользователя
     */
    @Override
    public boolean registerUser(String username, String password) {
        System.out.println("[Auth] Регистрация: " + username);
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE current_username = ? OR original_username = ?";
        String insertUserSQL = "INSERT INTO users (original_username, current_username, password_hash) VALUES (?, ?, ?)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSQL);
             PreparedStatement insertStmt = connection.prepareStatement(insertUserSQL)) {

            checkStmt.setString(1, username);
            checkStmt.setString(2, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.getInt(1) > 0) {
                System.out.println("[Auth] Логин занят: " + username);
                return false;
            }

            String salt = generateSaltFromUsername(username);
            String passwordHash = hashPassword(password, salt);

            insertStmt.setString(1, username);
            insertStmt.setString(2, username);
            insertStmt.setString(3, passwordHash);
            insertStmt.executeUpdate();

            System.out.println("[Auth] Пользователь создан: " + username);
            return true;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка регистрации: " + e.getMessage());
            throw new RuntimeException("Ошибка регистрации пользователя", e);
        }
    }

    /**
     * Аутентифицирует пользователя по логину и паролю
     */
    @Override
    public boolean authenticate(String username, String password) {
        System.out.println("[Auth] Аутентификация: " + username);
        String sql = "SELECT original_username, password_hash FROM users WHERE current_username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String originalUsername = rs.getString("original_username");
                String storedHash = rs.getString("password_hash");

                String salt = generateSaltFromUsername(originalUsername);
                String computedHash = hashPassword(password, salt);
                boolean result = storedHash.equals(computedHash);

                System.out.println("[Auth] Результат: " + (result ? "успех" : "неверный пароль"));
                return result;
            }
            System.out.println("[Auth] Пользователь не найден");
            return false;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка аутентификации: " + e.getMessage());
            throw new RuntimeException("Ошибка аутентификации", e);
        }
    }

    /**
     * Сбрасывает пароль пользователя
     */
    @Override
    public boolean resetPassword(String username, String newPassword) {
        System.out.println("[Auth] Смена пароля: " + username);
        String sql = "UPDATE users SET password_hash = ? WHERE current_username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String originalUsername = getOriginalUsername(username);
            if (originalUsername == null) {
                System.out.println("[Auth] Оригинальный логин не найден");
                return false;
            }

            String salt = generateSaltFromUsername(originalUsername);
            String newPasswordHash = hashPassword(newPassword, salt);

            stmt.setString(1, newPasswordHash);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            boolean result = rowsUpdated > 0;
            System.out.println("[Auth] Пароль изменен: " + result);
            return result;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка смены пароля: " + e.getMessage());
            throw new RuntimeException("Ошибка сброса пароля", e);
        }
    }

    /**
     * Изменяет логин пользователя
     */
    @Override
    public boolean changeUsername(String oldUsername, String newUsername) {
        System.out.println("[Auth] Смена логина: " + oldUsername + " -> " + newUsername);
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE current_username = ? OR original_username = ?";
        String updateUsernameSQL = "UPDATE users SET current_username = ? WHERE current_username = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSQL);
             PreparedStatement updateStmt = connection.prepareStatement(updateUsernameSQL)) {

            checkStmt.setString(1, newUsername);
            checkStmt.setString(2, newUsername);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.getInt(1) > 0) {
                System.out.println("[Auth] Новый логин занят");
                return false;
            }

            updateStmt.setString(1, newUsername);
            updateStmt.setString(2, oldUsername);

            int rowsUpdated = updateStmt.executeUpdate();
            boolean result = rowsUpdated > 0;
            System.out.println("[Auth] Логин изменен: " + result);
            return result;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка смены логина: " + e.getMessage());
            throw new RuntimeException("Ошибка изменения логина", e);
        }
    }

    /**
     * Получает оригинальный (первый) логин пользователя
     */
    @Override
    public String getOriginalUsername(String currentUsername) {
        String sql = "SELECT original_username FROM users WHERE current_username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("original_username") : null;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка получения оригинального логина: " + e.getMessage());
            throw new RuntimeException("Ошибка получения оригинального логина", e);
        }
    }

    /**
     * Привязывает Telegram chat ID к учетной записи
     */
    @Override
    public boolean linkTelegramChat(String username, long telegramChatId) {
        System.out.println("[Auth] Привязка Telegram: " + username + " -> " + telegramChatId);
        String sql = "UPDATE users SET telegram_chat_id = ? WHERE current_username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, telegramChatId);
            stmt.setString(2, username);
            int rowsUpdated = stmt.executeUpdate();
            boolean result = rowsUpdated > 0;
            System.out.println("[Auth] Telegram привязан: " + result);
            return result;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка привязки Telegram: " + e.getMessage());
            throw new RuntimeException("Ошибка привязки Telegram chat ID", e);
        }
    }

    /**
     * Привязывает Discord channel ID к учетной записи
     */
    @Override
    public boolean linkDiscordChannel(String username, long discordChannelId) {
        System.out.println("[Auth] Привязка Discord: " + username + " -> " + discordChannelId);
        String sql = "UPDATE users SET discord_channel_id = ? WHERE current_username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, discordChannelId);
            stmt.setString(2, username);
            int rowsUpdated = stmt.executeUpdate();
            boolean result = rowsUpdated > 0;
            System.out.println("[Auth] Discord привязан: " + result);
            return result;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка привязки Discord: " + e.getMessage());
            throw new RuntimeException("Ошибка привязки Discord channel ID", e);
        }
    }

    /**
     * Проверяет авторизацию пользователя по Telegram chat ID
     */
    @Override
    public boolean isTelegramUserAuthorized(long telegramChatId) {
        String sql = "SELECT COUNT(*) FROM users WHERE telegram_chat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, telegramChatId);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка проверки Telegram: " + e.getMessage());
            throw new RuntimeException("Ошибка проверки авторизации Telegram", e);
        }
    }

    /**
     * Проверяет авторизацию пользователя по Discord channel ID
     */
    @Override
    public boolean isDiscordUserAuthorized(long discordChannelId) {
        String sql = "SELECT COUNT(*) FROM users WHERE discord_channel_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, discordChannelId);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка проверки Discord: " + e.getMessage());
            throw new RuntimeException("Ошибка проверки авторизации Discord", e);
        }
    }

    /**
     * Получает логин пользователя по Telegram chat ID
     */
    @Override
    public String getUsernameByTelegramChatId(long telegramChatId) {
        String sql = "SELECT current_username FROM users WHERE telegram_chat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, telegramChatId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("current_username") : null;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка получения по Telegram ID: " + e.getMessage());
            throw new RuntimeException("Ошибка получения пользователя по Telegram ID", e);
        }
    }

    /**
     * Получает логин пользователя по Discord channel ID
     */
    @Override
    public String getUsernameByDiscordChannelId(long discordChannelId) {
        String sql = "SELECT current_username FROM users WHERE discord_channel_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, discordChannelId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("current_username") : null;

        } catch (SQLException e) {
            System.err.println("[Auth] Ошибка получения по Discord ID: " + e.getMessage());
            throw new RuntimeException("Ошибка получения пользователя по Discord ID", e);
        }
    }

    /**
     * Генерирует соль на основе имени пользователя
     */
    private String generateSaltFromUsername(String username) {
        try {
            // Получаем экземпляр алгоритма SHA-256 для хеширования
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Преобразуем строку в байты и хешируем алгоритмом SHA-256
            byte[] salt = md.digest((username + "GLOBE_TALK_SALT").getBytes());
            // Кодируем бинарный хеш в Base64
            // Base64 преобразует байты в текстовую строку
            return Base64.getEncoder().encodeToString(salt);
        } catch (NoSuchAlgorithmException e) {
            // Обработка случая, когда SHA-256 недоступен в системе
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Хеширует пароль с солью
     */
    private String hashPassword(String password, String salt) {
        try {
            // Получаем экземпляр алгоритма SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Декодируем соль из Base64 обратно в бинарный формат
            // Base64.getDecoder().decode() преобразует текстовую строку в байты
            // md.update() добавляет эти байты в хеш-функцию
            md.update(Base64.getDecoder().decode(salt));
            // Хешируем пароль: преобразуем строку пароля в байты
            // и передаем в хеш-функцию, которая уже содержит соль
            // md.digest() завершает хеширование и возвращает результат
            byte[] hashedPassword = md.digest(password.getBytes());
            // Кодируем финальный хеш в Base64 для хранения в БД
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Закрывает подключение к базе данных
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[Data Base] Ошибка закрытия: " + e.getMessage());
            throw new RuntimeException("Ошибка закрытия подключения к базе данных", e);
        }
    }
}