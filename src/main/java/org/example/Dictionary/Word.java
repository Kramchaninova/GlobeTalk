package org.example.Dictionary;

/**
 * Word - класс представляющий слово в словаре.
 */
public class Word {
    private final int id;
    private final long userId;
    private final String englishWord;
    private final String translation;
    private final int priority;

    public Word(int id, long userId, String englishWord, String translation, int priority) {
        this.id = id;
        this.userId = userId;
        this.englishWord = englishWord;
        this.translation = translation;
        this.priority = priority;
    }

    // Геттеры
    public int getId() { return id; }
    public long getUserId() { return userId; }  // long вместо int
    public String getEnglishWord() { return englishWord; }
    public String getTranslation() { return translation; }
    public int getPriority() { return priority; }

    @Override
    public String toString() {
        return "• " + englishWord + " - " + translation;
    }
}