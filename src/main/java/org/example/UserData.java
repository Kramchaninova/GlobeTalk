package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Представляет состояние прохождения теста конкретным пользователем.
 * Содержит:
 * список вопросов текущего теста,
 * список правильных ответов,
 * текущий индекс вопроса,
 * текущий набор баллов пользователя,
 * количество баллов за каждый вопрос.
 */
public class UserData {
    private List<String> currentTest = new ArrayList<>();
    private List<String> correctAnswers = new ArrayList<>();
    private int currentIndex = 0;
    private int totalScore = 0;
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
