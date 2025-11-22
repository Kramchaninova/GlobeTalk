package org.example.ScheduledTests;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для хранения состояния теста пользователя
 * Отслеживает текущий вопрос, правильные ответы и распределяет слова по результатам
 */
public class TestSession {
    private final TestsData testsData;
    private final long userId;
    private int currentQuestionIndex;
    private int correctAnswersCount;

    // Списки для распределения слов по результатам теста
    private final List<String> priorityCorrectWords = new ArrayList<>();
    private final List<String> priorityWrongWords = new ArrayList<>();
    private final List<String> newCorrectWords = new ArrayList<>();
    private final List<String> newWrongWords = new ArrayList<>();

    // Списки переводов для отчетности
    private final List<String> priorityCorrectTranslations = new ArrayList<>();
    private final List<String> priorityWrongTranslations = new ArrayList<>();
    private final List<String> newCorrectTranslations = new ArrayList<>();
    private final List<String> newWrongTranslations = new ArrayList<>();

    /**
     * Создает новую сессию теста
     * @param testsData данные теста
     * @param userId идентификатор пользователя
     */
    public TestSession(TestsData testsData, long userId) {
        this.testsData = testsData;
        this.userId = userId;
        this.currentQuestionIndex = 0;
        this.correctAnswersCount = 0;
    }

    /**
     * Возвращает текущий вопрос с номером
     */
    public String getCurrentQuestion() {
        if (currentQuestionIndex >= testsData.getQuestions().size()) {
            return "Тест завершен.";
        }

        TestsData.QuestionData question = testsData.getQuestions().get(currentQuestionIndex);
        int questionNumber = currentQuestionIndex + 1;
        int totalQuestions = testsData.getQuestions().size();

        return "Вопрос " + questionNumber + " из " + totalQuestions + ":\n\n" +
                question.getQuestionText();
    }

    /**
     * Проверяет ответ пользователя и распределяет слово по спискам
     * @param userAnswer ответ пользователя (A-D)
     * @return true если ответ правильный
     */
    public boolean checkAnswer(String userAnswer) {
        TestsData.QuestionData currentQuestion = testsData.getQuestions().get(currentQuestionIndex);
        boolean isCorrect = currentQuestion.getCorrectAnswer().equalsIgnoreCase(userAnswer);

        // Распределяем слово по соответствующему списку
        String englishWord = currentQuestion.getEnglishWord();
        String translation = currentQuestion.getTranslation();
        boolean isPriorityWord = "ПРИОРИТЕТНОЕ".equals(currentQuestion.getWordType());

        if (isPriorityWord) {
            if (isCorrect) {
                priorityCorrectWords.add(englishWord);
                priorityCorrectTranslations.add(translation);
                System.out.println("correct priority word");
            } else {
                priorityWrongWords.add(englishWord);
                priorityWrongTranslations.add(translation);
                System.out.println("Not correct priority word");
            }
        } else {
            if (isCorrect) {
                newCorrectWords.add(englishWord);
                newCorrectTranslations.add(translation);
                System.out.println("correct word");
            } else {
                newWrongWords.add(englishWord);
                newWrongTranslations.add(translation);
                System.out.println("Not correct word");
            }
        }

        if (isCorrect) {
            correctAnswersCount++;
        }

        return isCorrect;
    }

    /**
     * Переходит к следующему вопросу
     */
    public void nextQuestion() {
        currentQuestionIndex++;
    }

    /**
     * Проверяет завершен ли тест
     */
    public boolean isTestCompleted() {
        return currentQuestionIndex >= testsData.getQuestions().size();
    }

    /**
     * Возвращает общее количество вопросов
     */
    public int getTotalQuestions() {
        return testsData.getQuestions().size();
    }

    /**
     * Возвращает количество правильных ответов
     */
    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    /**
     * Возвращает данные текущего вопроса
     */
    public TestsData.QuestionData getCurrentQuestionData() {
        if (currentQuestionIndex < testsData.getQuestions().size()) {
            return testsData.getQuestions().get(currentQuestionIndex);
        }
        return null;
    }

    // Геттеры для списков слов

    /** Возвращает приоритетные слова с правильными ответами */
    public List<String> getPriorityCorrectWords() { return priorityCorrectWords; }

    /** Возвращает приоритетные слова с неправильными ответами */
    public List<String> getPriorityWrongWords() { return priorityWrongWords; }

    /** Возвращает новые слова с правильными ответами */
    public List<String> getNewCorrectWords() { return newCorrectWords; }

    /** Возвращает новые слова с неправильными ответами */
    public List<String> getNewWrongWords() { return newWrongWords; }

    /** Возвращает переводы приоритетных слов с правильными ответами */
    public List<String> getPriorityCorrectTranslations() { return priorityCorrectTranslations; }

    /** Возвращает переводы приоритетных слов с неправильными ответами */
    public List<String> getPriorityWrongTranslations() { return priorityWrongTranslations; }

    /** Возвращает переводы новых слов с правильными ответами */
    public List<String> getNewCorrectTranslations() { return newCorrectTranslations; }

    /** Возвращает переводы новых слов с неправильными ответами */
    public List<String> getNewWrongTranslations() { return newWrongTranslations; }

    /** Возвращает идентификатор пользователя */
    public long getUserId() { return userId; }
}