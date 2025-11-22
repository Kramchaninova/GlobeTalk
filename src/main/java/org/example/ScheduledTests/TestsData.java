package org.example.ScheduledTests;

import java.util.ArrayList;
import java.util.List;

public class TestsData {
    private List<QuestionData> questions;

    public TestsData() {
        this.questions = new ArrayList<>();
    }

    public void addQuestion(QuestionData question) {
        questions.add(question);
    }

    public List<QuestionData> getQuestions() {
        return questions;
    }

    public static class QuestionData {
        private String questionText; // Полный текст вопроса с вариантами
        private String correctAnswer; // Только буква (A, B, C, D)
        private String wordType; // "ПРИОРИТЕТНОЕ" или "НОВОЕ"
        private String englishWord; // Слово на английском
        private String translation; // Перевод слова

        public QuestionData(String questionText, String correctAnswer,
                            String wordType, String englishWord, String translation) {
            this.questionText = questionText;
            this.correctAnswer = correctAnswer;
            this.wordType = wordType;
            this.englishWord = englishWord;
            this.translation = translation;
        }

        // Геттеры
        public String getQuestionText() { return questionText; }
        public String getCorrectAnswer() { return correctAnswer; }
        public String getWordType() { return wordType; }
        public String getEnglishWord() { return englishWord; }
        public String getTranslation() { return translation; }
    }
}