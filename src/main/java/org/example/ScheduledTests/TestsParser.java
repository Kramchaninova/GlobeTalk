package org.example.ScheduledTests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер для преобразования текста теста в структурированные данные
 */
public class TestsParser {

    /**
     * Парсит текст теста и возвращает TestsData
     */
    public TestsData parseTest(String testText) {
        TestsData testsData = new TestsData();

        if (testText == null || testText.isEmpty()) {
            return testsData;
        }

        // Разделяем текст на вопросы
        String[] questionBlocks = testText.split("Вопрос:");

        for (String block : questionBlocks) {
            if (block.trim().isEmpty()) continue;

            TestsData.QuestionData questionData = parseQuestionBlock(block.trim());
            if (questionData != null) {
                testsData.addQuestion(questionData);
            }
        }

        return testsData;
    }

    /**
     * Парсит блок одного вопроса
     */
    private TestsData.QuestionData parseQuestionBlock(String block) {
        try {
            String questionText = extractFullQuestionText(block);
            String correctAnswer = extractCorrectAnswer(block);
            String wordType = extractWordType(block);
            String[] wordAndTranslation = extractWordAndTranslation(block);

            if (questionText != null && correctAnswer != null) {
                return new TestsData.QuestionData(
                        questionText,
                        correctAnswer,
                        wordType,
                        wordAndTranslation[0],
                        wordAndTranslation[1]
                );
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга блока вопроса: " + e.getMessage());
        }

        return null;
    }

    /**
     * Извлекает полный текст вопроса с вариантами
     */
    private String extractFullQuestionText(String block) {
        Pattern pattern = Pattern.compile("^(.*?)(?=Ответ:)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(block);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    /**
     * Извлекает правильный ответ (A-D)
     */
    private String extractCorrectAnswer(String block) {
        Pattern pattern = Pattern.compile("Ответ:\\s*([A-D])");
        Matcher matcher = pattern.matcher(block);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Извлекает тип слова (ПРИОРИТЕТНОЕ/НОВОЕ)
     */
    private String extractWordType(String block) {
        Pattern pattern = Pattern.compile("Тип:\\s*(ПРИОРИТЕТНОЕ|НОВОЕ)");
        Matcher matcher = pattern.matcher(block);
        return matcher.find() ? matcher.group(1) : "НЕИЗВЕСТНО";
    }

    /**
     * Извлекает слово и перевод
     */
    private String[] extractWordAndTranslation(String block) {
        Pattern pattern = Pattern.compile("Словo:\\s*(.*?)\\s*-\\s*(.*?)(?=\\s*$|\\s*Вопрос:)");
        Matcher matcher = pattern.matcher(block);
        return matcher.find() ?
                new String[]{matcher.group(1).trim(), matcher.group(2).trim()} :
                new String[]{"", ""};
    }
}