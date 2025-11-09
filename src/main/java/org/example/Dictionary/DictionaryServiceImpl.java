package org.example.Dictionary;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DictionaryServiceImpl - реализация работы со словарем в SQLite.
 * Выполняет CRUD операции с базой данных слов.
 */
public class DictionaryServiceImpl implements DictionaryService {
    private Connection connection;

    /**
     * Конструктор инициализирует соединение с базой данных и создает таблицу при необходимости.
     */
    public DictionaryServiceImpl() {
        initDatabase();
    }

    /**
     * Инициализирует соединение с базой данных SQLite и создает таблицу.
     */
    private void initDatabase() {
        try {
            String url = "jdbc:sqlite:dictionary.db";
            connection = DriverManager.getConnection(url);
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Создает таблицу dictionary если она не существует.
     * Таблица содержит поля: id, user_id, english_word, translation, priority, created_at
     */
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS dictionary (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id BIGINT NOT NULL,
                english_word TEXT NOT NULL,
                translation TEXT NOT NULL,
                priority INTEGER DEFAULT 2,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Добавляет новое слово в словарь пользователя.
     *
     * @param userId идентификатор пользователя
     * @param englishWord английское слово
     * @param translation перевод слова
     * @param priority приоритет слова (1-низкий, 2-средний, 3-высокий)
     * @throws SQLException если произошла ошибка при работе с базой данных
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
        }
    }

    /**
     * Получает все слова пользователя из словаря.
     *
     * @param userId идентификатор пользователя
     * @return список слов пользователя
     * @throws SQLException если произошла ошибка при работе с базой данных
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
        return words;
    }

    /**
     * Находит слово по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @param wordId идентификатор слова
     * @return найденное слово или null если слово не найдено
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    @Override
    public Word getWordById(long userId, int wordId) throws SQLException {
        String sql = "SELECT * FROM dictionary WHERE user_id = ? AND id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setInt(2, wordId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Word(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("english_word"),
                        rs.getString("translation"),
                        rs.getInt("priority")
                );
            }
        }
        return null;
    }

    /**
     * Находит слово по английскому написанию.
     *
     * @param userId идентификатор пользователя
     * @param englishWord английское слово для поиска
     * @return найденное слово или null если слово не найдено
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    @Override
    public Word getWordByEnglish(long userId, String englishWord) throws SQLException {
        String sql = "SELECT * FROM dictionary WHERE user_id = ? AND english_word = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, englishWord);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Word(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("english_word"),
                        rs.getString("translation"),
                        rs.getInt("priority")
                );
            }
        }
        return null;
    }

    /**
     * Обновляет данные слова в словаре.
     *
     * @param userId идентификатор пользователя
     * @param wordId идентификатор слова
     * @param newEnglishWord новое английское слово
     * @param newTranslation новый перевод
     * @param newPriority новый приоритет
     * @throws SQLException если произошла ошибка при работе с базой данных
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
        }
    }

    /**
     * Удаляет слово из словаря пользователя.
     *
     * @param userId идентификатор пользователя
     * @param wordId идентификатор слова для удаления
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    @Override
    public void deleteWord(long userId, int wordId) throws SQLException {
        String sql = "DELETE FROM dictionary WHERE user_id = ? AND id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setInt(2, wordId);
            pstmt.executeUpdate();
        }
    }

}