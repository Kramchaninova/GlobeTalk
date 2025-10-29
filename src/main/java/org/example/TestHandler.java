package org.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Обработка логики теста:
 * Хранит вопросы, ответы, текущий индекс и количество набранных баллов для каждого пользователя.
 * Отвечает за генерацию теста из текстовой строки, обработку ответов пользователей
 * подсчёт баллов и определение уровня владения языком, создание inline-клавиатуры для ответов A/B/C/D.
 */

public class TestHandler {

    // Храним данные для каждого пользователя
    private final Map<Long, UserData> users = new HashMap<>();

    private static final String ANSWER_ERROR = "Не удалось распознать вопросы в тесте.";
    private static final String AGAIN_TEST = "Сначала начните тест командой /start.";

    /**
     * Разбирает текст теста, извлекает вопросы, варианты ответов и правильные ответы.
     * Сохраняет данные для конкретного пользователя и инициализирует индекс текущего вопроса
     * и общий счёт. Возвращает первый вопрос для отображения.
     *
     */

    public String generateTest(long chatId, String test) {
        Pattern pattern = Pattern.compile(
                "(\\d+).?\\s*\\((\\d+)\\s*[points]*\\)\\s*\\n" +
                        "(.+?)\\n" +
                        "(A\\..+?)\\n" +
                        "(B\\..+?)\\n" +
                        "(C\\..+?)\\n" +
                        "(D\\..+?)\\n" +
                        "Answer:\\s*?([A-D])",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(test);
        List<String> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        List<Integer> pointsList = new ArrayList<>();

        // Обрабатываем каждый вопрос
        while (matcher.find()) {
            String points = matcher.group(2);
            String question = matcher.group(3).trim();
            String answerA = matcher.group(4).trim();
            String answerB = matcher.group(5).trim();
            String answerC = matcher.group(6).trim();
            String answerD = matcher.group(7).trim();
            String correctAnswer = matcher.group(8).trim();

            // Сохраняем только текст вопроса без номера и баллов
            String questionText = question + "\n" +
                    answerA + "\n" +
                    answerB + "\n" +
                    answerC + "\n" +
                    answerD;

            questions.add(questionText);
            answers.add(correctAnswer);
            pointsList.add(Integer.parseInt(points));
        }

        if (questions.isEmpty()) {
            return ANSWER_ERROR;
        }

        UserData userData = new UserData();
        userData.setCurrentTest(questions);
        userData.setCorrectAnswers(answers);
        userData.setQuestionPoints(pointsList);
        userData.setCurrentIndex(0);
        userData.setTotalScore(0);

        users.put(chatId, userData);

        return questions.getFirst();
    }

    /**
     * Обрабатывает выбор ответа пользователя на вопрос теста (кнопки A/B/C/D).
     * Проверяет правильность ответа, начисляет баллы и возвращает следующий вопрос.
     * Если вопросы закончились, подсчитывает итоговый результат и уровень владения языком.
     */

    public String handleAnswer(String callbackData, long chatId) {
        UserData userData = users.get(chatId);

        if (userData == null) {
            return AGAIN_TEST;
        }

        String chosen = callbackData.substring(0, 1);

        int index = userData.getCurrentIndex();
        List<String> correctAnswers = userData.getCorrectAnswers();
        List<Integer> pointsList = userData.getQuestionPoints();

        if (correctAnswers.get(index).equalsIgnoreCase(chosen)) {
            userData.setTotalScore(userData.getTotalScore() + pointsList.get(index));
        }

        userData.setCurrentIndex(index + 1);

        if (userData.getCurrentIndex() >= userData.getCurrentTest().size()) {

            int totalPoints = pointsList.stream().mapToInt(Integer::intValue).sum();
            int earnedPoints = userData.getTotalScore();

            users.remove(chatId); // очищаем полностью

            String languageLevel;
            if (earnedPoints <= 6) {
                languageLevel = "A1-A2 (Начальный)";
            } else if (earnedPoints <= 12) {
                languageLevel = "B1-B2 (Средний)";
            } else {
                languageLevel = "C1-C2 (Продвинутый)";
            }

            return "🎉 *Тест завершён!* 🎉\n\n" +

                    "📊 **Результаты тестирования:**\n" +
                    "🏆 Набрано баллов: " + earnedPoints + " из " + totalPoints + " возможных\n" +
                    "📈 Уровень владения языком: " + languageLevel + "\n\n" +

                    "✨ **Отличная работа!** ✨\n\n";
        }

        return userData.getCurrentTest().get(userData.getCurrentIndex());
    }

    /**
     * Проверяет, активен ли тест для пользователя.
     */

    public boolean isTestActive(long chatId) {
        UserData userData = users.get(chatId);
        return userData != null &&
                userData.getCurrentIndex() < userData.getCurrentTest().size();
    }

}
