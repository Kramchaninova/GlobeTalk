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
     * @return текст вопроса с номером или сообщение о завершении
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

        System.out.println("\n\n        TestSession.checkAnswer()          ");
        System.out.println("Английское слово: '" + currentQuestion.getEnglishWord() + "'");
        System.out.println("Перевод: '" + currentQuestion.getTranslation() + "'");
        System.out.println("Тип слова: " + currentQuestion.getWordType());
        System.out.println("Правильный ответ: " + currentQuestion.getCorrectAnswer());
        System.out.println("Ответ пользователя: " + userAnswer);
        System.out.println("Результат: " + (isCorrect ? "ПРАВИЛЬНО" : "НЕПРАВИЛЬНО"));

        // Проверка на пустые слова
        if (currentQuestion.getEnglishWord() == null || currentQuestion.getEnglishWord().trim().isEmpty()) {
            System.err.println("❌ ОШИБКА: Пустое английское слово!");
            return isCorrect;
        }
        if (currentQuestion.getTranslation() == null || currentQuestion.getTranslation().trim().isEmpty()) {
            System.err.println("❌ ОШИБКА: Пустой перевод!");
            return isCorrect;
        }

        // Распределяем слово по соответствующему списку
        String englishWord = currentQuestion.getEnglishWord();
        String translation = currentQuestion.getTranslation();
        boolean isPriorityWord = "ПРИОРИТЕТНОЕ".equals(currentQuestion.getWordType());

        if (isPriorityWord) {
            if (isCorrect) {
                priorityCorrectWords.add(englishWord);
                priorityCorrectTranslations.add(translation);
            } else {
                priorityWrongWords.add(englishWord);
                priorityWrongTranslations.add(translation);
            }
        } else {
            if (isCorrect) {
                newCorrectWords.add(englishWord);
                newCorrectTranslations.add(translation);
            } else {
                newWrongWords.add(englishWord);
                newWrongTranslations.add(translation);
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
     * @return true если тест завершен
     */
    public boolean isTestCompleted() {
        return currentQuestionIndex >= testsData.getQuestions().size();
    }

    /**
     * Возвращает общее количество вопросов
     * @return количество вопросов в тесте
     */
    public int getTotalQuestions() {
        return testsData.getQuestions().size();
    }

    /**
     * Возвращает количество правильных ответов
     * @return число правильных ответов
     */
    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    /**
     * Возвращает данные текущего вопроса
     * @return текущий вопрос или null если тест завершен
     */
    public TestsData.QuestionData getCurrentQuestionData() {
        if (currentQuestionIndex < testsData.getQuestions().size()) {
            return testsData.getQuestions().get(currentQuestionIndex);
        }
        return null;
    }

    // Геттеры для списков слов

    /** @return приоритетные слова с правильными ответами */
    public List<String> getPriorityCorrectWords() { return priorityCorrectWords; }

    /** @return приоритетные слова с неправильными ответами */
    public List<String> getPriorityWrongWords() { return priorityWrongWords; }

    /** @return новые слова с правильными ответами */
    public List<String> getNewCorrectWords() { return newCorrectWords; }

    /** @return новые слова с неправильными ответами */
    public List<String> getNewWrongWords() { return newWrongWords; }

    /** @return переводы приоритетных слов с правильными ответами */
    public List<String> getPriorityCorrectTranslations() { return priorityCorrectTranslations; }

    /** @return переводы приоритетных слов с неправильными ответами */
    public List<String> getPriorityWrongTranslations() { return priorityWrongTranslations; }

    /** @return переводы новых слов с правильными ответами */
    public List<String> getNewCorrectTranslations() { return newCorrectTranslations; }

    /** @return переводы новых слов с неправильными ответами */
    public List<String> getNewWrongTranslations() { return newWrongTranslations; }

    /** @return идентификатор пользователя */
    public long getUserId() { return userId; }
}