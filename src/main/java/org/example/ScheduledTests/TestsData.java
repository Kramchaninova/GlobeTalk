package org.example.ScheduledTests;

import java.util.ArrayList;
import java.util.List;

/**
 * Контейнер для хранения данных теста
 * Содержит список вопросов и их метаданные
 */
public class TestsData {
    private List<QuestionData> questions;

    /**
     * Создает новый контейнер для данных теста
     */
    public TestsData() {
        this.questions = new ArrayList<>();
    }

    /**
     * Добавляет вопрос в тест
     * @param question данные вопроса
     */
    public void addQuestion(QuestionData question) {
        questions.add(question);
    }

    /**
     * @return список всех вопросов теста
     */
    public List<QuestionData> getQuestions() {
        return questions;
    }

    /**
     * Внутренний класс для хранения данных одного вопроса
     */
    public static class QuestionData {
        private String questionText; // Полный текст вопроса с вариантами
        private String correctAnswer; // Только буква (A, B, C, D)
        private String wordType; // "ПРИОРИТЕТНОЕ" или "НОВОЕ"
        private String englishWord; // Слово на английском
        private String translation; // Перевод слова

        /**
         * Создает новый вопрос
         * @param questionText полный текст вопроса с вариантами
         * @param correctAnswer правильный ответ (A, B, C, D)
         * @param wordType тип слова (ПРИОРИТЕТНОЕ/НОВОЕ)
         * @param englishWord английское слово
         * @param translation перевод слова
         */
        public QuestionData(String questionText, String correctAnswer,
                            String wordType, String englishWord, String translation) {
            this.questionText = questionText;
            this.correctAnswer = correctAnswer;
            this.wordType = wordType;
            this.englishWord = englishWord;
            this.translation = translation;
        }

        /** @return полный текст вопроса с вариантами ответов */
        public String getQuestionText() { return questionText; }

        /** @return правильный ответ (A, B, C, D) */
        public String getCorrectAnswer() { return correctAnswer; }

        /** @return тип слова (ПРИОРИТЕТНОЕ/НОВОЕ) */
        public String getWordType() { return wordType; }

        /** @return английское слово */
        public String getEnglishWord() { return englishWord; }

        /** @return перевод слова */
        public String getTranslation() { return translation; }
    }
}