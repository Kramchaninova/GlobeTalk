package org.example.Dictionary;

/**
 * Word - представляет слово в словаре.
 * Содержит оригинал, перевод и приоритет изучения.
 */
public class Word {
    private int id;
    private long userId;
    private String englishWord;
    private String translation;
    private int priority = 1; //пока стоит фикс один, потом уже будет наверное 10 + абработка чтобы приоритет не был меньше 0

    /**
     * Конструктор для создания объекта слова.
     *
     * @param id уникальный идентификатор слова
     * @param userId идентификатор пользователя-владельца слова
     * @param englishWord слово на английском языке
     * @param translation перевод слова
     * @param priority приоритет изучения (1=низкий, 2=средний, 3=высокий)
     */
    public Word(int id, long userId, String englishWord, String translation, int priority) {
        this.id = id;
        this.userId = userId;
        this.englishWord = englishWord;
        this.translation = translation;
        this.priority = priority;
    }

    /**
     * Возвращает идентификатор слова.
     * @return идентификатор слова
     */
    public int getId() {
        return id;
    }

    /**
     * Возвращает слово на английском языке.
     * @return английское слово
     */
    public String getEnglishWord() {
        return englishWord;
    }

    /**
     * Возвращает перевод слова.
     * @return перевод слова
     */
    public String getTranslation() {
        return translation;
    }


    /**
     * Возвращает приоритет изучения слова.
     * @return приоритет изучения
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Возвращает строковое представление слова в формате: "английское_слово - перевод".
     * Используется для отображения слова в списке словаря.
     * @return строковое представление слова
     */
    @Override
    public String toString() {
        return englishWord + " - " + translation;
    }
}