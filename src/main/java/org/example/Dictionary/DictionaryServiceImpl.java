package org.example.Dictionary;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DictionaryServiceImpl - реализация работы со словарем в SQLite.
 * Выполняет Create, Read, Update, Delete операции с базой данных слов.
 */
public class DictionaryServiceImpl implements DictionaryService {
    private Connection connection;

    /**
     * Конструктор - инициализирует БД и создает таблицы
     */
    public DictionaryServiceImpl() {
        initDatabase();
    }

    /**
     * Инициализирует соединение с БД SQLite
     */
    private void initDatabase() {
        try {
            String url = "jdbc:sqlite:dictionary.db";
            connection = DriverManager.getConnection(url);
            createTable();
        } catch (SQLException e) {
            System.err.println("[Dictionary] Ошибка инициализации БД: " + e.getMessage());
        }
    }

    /**
     * Создает таблицу dictionary если она не существует
     */
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS dictionary (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id BIGINT NOT NULL,
                english_word TEXT NOT NULL,
                translation TEXT NOT NULL,
                priority INTEGER NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("[Dictionary] Таблица dictionary создана/проверена");
        } catch (SQLException e) {
            System.err.println("[Dictionary] Ошибка создания таблицы: " + e.getMessage());
        }
    }

    /**
     * Получает ID пользователя из БД аутентификации по chatId
     *
     * @param chatId идентификатор чата пользователя
     * @return ID пользователя из системы аутентификации
     * @throws SQLException если пользователь не найден или произошла ошибка БД
     */
    @Override
    public long getUserIdByChatId(long chatId) throws SQLException {
        String authDbUrl = "jdbc:sqlite:bot_auth.db";
        try (Connection authConn = DriverManager.getConnection(authDbUrl);
             PreparedStatement pstmt = authConn.prepareStatement(
                     "SELECT id FROM users WHERE telegram_chat_id = ? OR discord_channel_id = ?")) {

            pstmt.setLong(1, chatId);
            pstmt.setLong(2, chatId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                System.out.println("[Dictionary] Найден userId: " + userId + " для chatId: " + chatId);
                return userId;
            }
            throw new SQLException("Пользователь не найден для chatId: " + chatId);

        } catch (SQLException e) {
            System.err.println("[Dictionary] Ошибка получения userId: " + e.getMessage());
            throw new SQLException("Не удалось найти пользователя в системе аутентификации", e);
        }
    }

    /**
     * Добавляет новое слово в словарь пользователя
     * @param userId идентификатор пользователя
     * @param englishWord английское слово
     * @param translation перевод слова
     * @param priority приоритет слова (1-низкий, 2-средний, 3-высокий)
     * @throws SQLException если произошла ошибка при работе с БД
     */
    @Override
    public void addWord(long userId, String englishWord, String translation, int priority) throws SQLException {
        String sql = "INSERT INTO dictionary (user_id, english_word, translation, priority) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, englishWord);
            pstmt.setString(3, translation);
            pstmt.setInt(4, priority);
            pstmt.executeUpdate();
            System.out.println("[Dictionary] Слово добавлено: " + englishWord + " для userId: " + userId);
        }
    }

    /**
     * Получает все слова пользователя из словаря
     * @param userId идентификатор пользователя
     * @return список слов пользователя
     * @throws SQLException если произошла ошибка при работе с БД
     */
    @Override
    public List<Word> getAllWords(long userId) throws SQLException {
        List<Word> words = new ArrayList<>();
        String sql = "SELECT * FROM dictionary WHERE user_id = ? ORDER BY id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Word word = new Word(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("english_word"),
                        rs.getString("translation"),
                        rs.getInt("priority")
                );
                words.add(word);
            }
        }
        System.out.println("[Dictionary] Загружено " + words.size() + " слов для userId: " + userId);
        return words;
    }

    /**
     * Находит слово по идентификатору
     * @param userId идентификатор пользователя
     * @param wordId идентификатор слова
     * @return найденное слово или null если слово не найдено
     * @throws SQLException если произошла ошибка при работе с БД
     */
    @Override
    public Word getWordById(long userId, int wordId) throws SQLException {
        String sql = "SELECT * FROM dictionary WHERE user_id = ? AND id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setInt(2, wordId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("[Dictionary] Найдено слово по ID: " + wordId + " для userId: " + userId);
                return new Word(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("english_word"),
                        rs.getString("translation"),
                        rs.getInt("priority")
                );
            }
        }
        System.out.println("[Dictionary] Слово не найдено по ID: " + wordId + " для userId: " + userId);
        return null;
    }

    /**
     * Находит слово по английскому написанию
     * @param userId идентификатор пользователя
     * @param englishWord английское слово для поиска
     * @return найденное слово или null если слово не найдено
     * @throws SQLException если произошла ошибка при работе с БД
     */
    @Override
    public Word getWordByEnglish(long userId, String englishWord) throws SQLException {
        String sql = "SELECT * FROM dictionary WHERE user_id = ? AND LOWER(english_word) = LOWER(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, englishWord.trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("[Dictionary] Найдено слово: '" + englishWord + "' -> '" +
                        rs.getString("english_word") + "' для userId: " + userId);
                return new Word(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("english_word"),
                        rs.getString("translation"),
                        rs.getInt("priority")
                );
            }
        }
        System.out.println("[Dictionary] Слово не найдено: '" + englishWord + "' для userId: " + userId);
        return null;
    }

    /**
     * Обновляет данные слова в словаре
     * @param userId идентификатор пользователя
     * @param wordId идентификатор слова
     * @param newEnglishWord новое английское слово
     * @param newTranslation новый перевод
     * @param newPriority новый приоритет (если null, используется DEFAULT_PRIORITY)
     * @throws SQLException если произошла ошибка при работе с БД
     */
    @Override
    public void updateWord(long userId, int wordId, String newEnglishWord, String newTranslation, Integer newPriority) throws SQLException {
        String sql = "UPDATE dictionary SET english_word = ?, translation = ?, priority = ? WHERE user_id = ? AND id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newEnglishWord);
            pstmt.setString(2, newTranslation);
            pstmt.setInt(3, newPriority != null ? newPriority : 2);
            pstmt.setLong(4, userId);
            pstmt.setInt(5, wordId);
            pstmt.executeUpdate();
            System.out.println("[Dictionary] Слово обновлено: " + newEnglishWord + " для userId: " + userId);
        }
    }

    /**
     * Удаляет слово из словаря пользователя
     * @param userId идентификатор пользователя
     * @param wordId идентификатор слова для удаления
     * @throws SQLException если произошла ошибка при работе с БД
     */
    @Override
    public void deleteWord(long userId, int wordId) throws SQLException {
        String sql = "DELETE FROM dictionary WHERE user_id = ? AND id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setInt(2, wordId);
            pstmt.executeUpdate();
            System.out.println("[Dictionary] Слово удалено: " + wordId + " для userId: " + userId);
        }
    }
    /**
     * Получает слова пользователя по приоритету
     */
    @Override
    public List<Word> getWordsByPriority(long userId, int priority) throws SQLException {
        List<Word> words = new ArrayList<>();
        String sql = "SELECT * FROM dictionary WHERE user_id = ? AND priority = ? ORDER BY id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setInt(2, priority);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Word word = new Word(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("english_word"),
                        rs.getString("translation"),
                        rs.getInt("priority")
                );
                words.add(word);
            }
        }
        System.out.println("[Dictionary] Загружено " + words.size() + " слов с приоритетом " + priority + " для userId: " + userId);
        return words;
    }
    /**
     * Обновляет приоритет слова в словаре
     * @param userId идентификатор пользователя
     * @param wordId идентификатор слова
     * @param newPriority новый приоритет (1-низкий, 2-средний, 3-высокий)
     * @throws SQLException если произошла ошибка при работе с БД
     */
    @Override
    public void updateWordPriority(long userId, int wordId, int newPriority) throws SQLException {
        String sql = "UPDATE dictionary SET priority = ? WHERE user_id = ? AND id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newPriority);
            pstmt.setLong(2, userId);
            pstmt.setInt(3, wordId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("[Dictionary] Приоритет слова обновлен: wordId=" + wordId + ", новый приоритет=" + newPriority + " для userId: " + userId);
            } else {
                throw new SQLException("Слово не найдено для обновления приоритета");
            }
        }
    }
}