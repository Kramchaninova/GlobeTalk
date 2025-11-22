package org.example.Dictionary;

import java.sql.SQLException;
import java.util.List;

/**
 * DictionaryService - интерфейс для работы с хранилищем слов.
 * Определяет контракт для операций со словарем.
 */
public interface DictionaryService {

    /**
     * Добавить слово с указанным приоритетом
     */
    void addWord(long userId, String englishWord, String translation, int priority) throws SQLException;

    /**
     * Получить все слова пользователя
     */
    List<Word> getAllWords(long userId) throws SQLException;

    /**
     * Получить слова пользователя по приоритету
     */
    List<Word> getWordsByPriority(long userId, int priority) throws SQLException;

    /**
     * Получить слово по ID
     */
    Word getWordById(long userId, int wordId) throws SQLException;

    /**
     * Найти слово по английскому варианту
     */
    Word getWordByEnglish(long userId, String englishWord) throws SQLException;

    /**
     * Обновить слово в словаре
     */
    void updateWord(long userId, int wordId, String newEnglishWord, String newTranslation, Integer newPriority) throws SQLException;

    /**
     * Обновить приоритет слова
     */
    void updateWordPriority(long userId, int wordId, int newPriority) throws SQLException;

    /**
     * Удалить слово из словаря
     */
    void deleteWord(long userId, int wordId) throws SQLException;

    /**
     * Получить ID пользователя из БД аутентификации по chatId
     */
    long getUserIdByChatId(long chatId) throws SQLException;
}