package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс хранит состояние прохождения теста для конкретного пользователя.
 * Отвечает за управление прогрессом пользователя во время тестирования.
 */
public class UserData {
    /** Список вопросов текущего теста */
    private List<String> currentTest = new ArrayList<>();
    /** Список правильных ответов на вопросы теста */
    private List<String> correctAnswers = new ArrayList<>();
    /** Текущий индекс вопроса в тесте */
    private int currentIndex = 0;
    /** Общее количество набранных баллов */
    private int totalScore = 0;
    /** Количество баллов за каждый вопрос теста */
    private List<Integer> questionPoints = new ArrayList<>();

    public List<String> getCurrentTest() {
        return currentTest;
    }

    public void setCurrentTest(List<String> currentTest) {
        this.currentTest = currentTest;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public List<Integer> getQuestionPoints() {
        return questionPoints;
    }

    public void setQuestionPoints(List<Integer> questionPoints) {
        this.questionPoints = questionPoints;
    }
}
