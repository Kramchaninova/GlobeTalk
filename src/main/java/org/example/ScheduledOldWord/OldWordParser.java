package org.example.ScheduledOldWord;

/**
 * Класс для парсинга сгенерированного теста
 */
public class OldWordParser {

    /**
     * Парсит сгенерированный тест и извлекает вопрос и правильный ответ
     */
    public OldWordData parseTest(String testText, String englishWord, String translation) {
        try {
            String fullQuestion = extractFullQuestion(testText);
            String correctAnswer = extractCorrectAnswer(testText);

            if (fullQuestion == null || correctAnswer == null) {
                throw new IllegalArgumentException("Не удалось распарсить тест: некорректный формат");
            }

            return new OldWordData(
                    fullQuestion,
                    correctAnswer,
                    englishWord,
                    translation
            );

        } catch (Exception e) {
            System.out.println("[LowPriorityTestParser] Ошибка парсинга теста: " + e.getMessage());
            throw new RuntimeException("Ошибка парсинга теста", e);
        }
    }

    /**
     * Извлекает весь вопрос с вариантами ответов (до "Ответ:")
     */
    private String extractFullQuestion(String testText) {
        int answerIndex = testText.indexOf("Ответ:");
        if (answerIndex != -1) {
            return testText.substring(0, answerIndex).trim();
        }
        return testText.trim(); // Если нет "Ответ:", возвращаем весь текст
    }

    /**
     * Извлекает правильный ответ
     */
    private String extractCorrectAnswer(String testText) {
        int answerIndex = testText.indexOf("Ответ:");
        if (answerIndex != -1) {
            String answerLine = testText.substring(answerIndex);
            String[] lines = answerLine.split("\n");
            if (lines.length > 0) {
                String answer = lines[0].replace("Ответ:", "").trim();
                return answer.toUpperCase(); // A, B, C, D в верхнем регистре
            }
        }
        return null;
    }
}