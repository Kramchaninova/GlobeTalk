package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранит состояние прохождения теста для пользователя.
 * Управляет прогрессом тестирования: вопросы, ответы, текущая позиция, баллы.
 */
public class UserData {
    /** Список вопросов текущего теста */
    private List<String> currentTest = new ArrayList<>();
    /** Список правильных ответов на вопросы теста */
    private List<String> correctAnswers = new ArrayList<>();
    /** Текущий индекс вопроса в тесте (0-based) */
    private int currentIndex = 0;
    /** Общее количество набранных баллов */
    private int totalScore = 0;
    /** Количество баллов за каждый вопрос теста */
    private List<Integer> questionPoints = new ArrayList<>();

    /**
     * @return список вопросов текущего теста
     */
    public List<String> getCurrentTest() {
        return currentTest;
    }

    /**
     * @param currentTest список вопросов для установки
     */
    public void setCurrentTest(List<String> currentTest) {
        this.currentTest = currentTest;
    }

    /**
     * @return список правильных ответов
     */
    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    /**
     * @param correctAnswers список правильных ответов для установки
     */
    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    /**
     * @return текущий индекс вопроса в тесте
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * @param currentIndex индекс текущего вопроса
     */
    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    /**
     * @param totalScore общее количество баллов
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * @return общее количество набранных баллов
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * @return список баллов за каждый вопрос
     */
    public List<Integer> getQuestionPoints() {
        return questionPoints;
    }

    /**
     * @param questionPoints список баллов за вопросы
     */
    public void setQuestionPoints(List<Integer> questionPoints) {
        this.questionPoints = questionPoints;
    }
}