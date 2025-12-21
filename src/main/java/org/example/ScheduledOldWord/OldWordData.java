package org.example.ScheduledOldWord;

/**
 * Класс для хранения данных о распарсенном тесте
 */
public class OldWordData {
    private final String fullQuestion;
    private final String correctAnswer;
    private final String englishWord;
    private final String translation;
    private int wordId;
    private int currentPriority;

    public OldWordData(String fullQuestion, String correctAnswer, String englishWord, String translation) {
        this.fullQuestion = fullQuestion;
        this.correctAnswer = correctAnswer;
        this.englishWord = englishWord;
        this.translation = translation;

        System.out.println("\n[OldWordData]   englishWord: " + englishWord);
        System.out.println("[OldWordData]   translation: " + translation);
        System.out.println("[OldWordData]   correctAnswer: " + correctAnswer);
        System.out.println("[OldWordData]   question: " + (fullQuestion != null ? fullQuestion : "null"));
    }

    // Геттеры
    public String getFullQuestion() { return fullQuestion; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getEnglishWord() { return englishWord; }
    public String getTranslation() { return translation; }
    public int getWordId() { return wordId; }
    public int getCurrentPriority() { return currentPriority; }

    // Сеттеры
    public void setWordId(int wordId) {
        this.wordId = wordId;
        System.out.println("[OldWordData] Установлен wordId: " + wordId);
    }

    public void setCurrentPriority(int currentPriority) {
        this.currentPriority = currentPriority;
        System.out.println("[OldWordData] Установлен currentPriority: " + currentPriority);
    }

    @Override
    public String toString() {
        return String.format(
                "ParsedTestData{\n" +
                        "  englishWord='%s',\n" +
                        "  translation='%s',\n" +
                        "  correctAnswer='%s',\n" +
                        "  wordId=%d,\n" +
                        "  currentPriority=%d,\n" +
                        "  questionLength=%d chars\n" +
                        "}",
                englishWord, translation, correctAnswer, wordId, currentPriority, fullQuestion.length()
        );
    }
}