package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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
    private final Map<Long, List<String>> currentTests = new HashMap<>();
    private final Map<Long, List<String>> correctAnswers = new HashMap<>();
    private final Map<Long, Integer> currentIndexes = new HashMap<>();
    private final Map<Long, Integer> totalScore = new HashMap<>();
    private final Map<Long, List<Integer>> questionPoints = new HashMap<>();

    private static final String ANSWER_ERROR = "Не удалось распознать вопросы в тесте.";
    private static final String AGAIN_TEST = "Сначала начните тест командой /start.";

    /**
     * Разбирает текст теста, извлекает вопросы, варианты ответов и правильные ответы.
     * Сохраняет данные для конкретного пользователя и инициализирует индекс текущего вопроса
     * и общий счёт. Возвращает первый вопрос для отображения.
     *
     * @param chatId
     * @param test
     * @return
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

        currentTests.put(chatId, questions);
        correctAnswers.put(chatId, answers);
        currentIndexes.put(chatId, 0);
        totalScore.put(chatId, 0);
        questionPoints.put(chatId, pointsList);

        return questions.get(0);
    }

    /**
     * Обрабатывает выбор ответа пользователя на вопрос теста (кнопки A/B/C/D).
     * Проверяет правильность ответа, начисляет баллы и возвращает следующий вопрос.
     * Если вопросы закончились, подсчитывает итоговый результат и уровень владения языком.
     */

    public String handleAnswer(String callbackData, long chatId) {
        if (!currentTests.containsKey(chatId)) {
            return AGAIN_TEST;
        }

        String chosen = callbackData.substring(0, 1);
        int index = currentIndexes.get(chatId);
        List<String> correct = correctAnswers.get(chatId);
        int score = totalScore.get(chatId);
        List<Integer> pointsList = questionPoints.get(chatId);

        // Проверяем ответ и начисляем баллы
        if (correct.get(index).equalsIgnoreCase(chosen)) {
            score += pointsList.get(index);
            totalScore.put(chatId, score);
        }

        index++;
        List<String> questions = currentTests.get(chatId);

        if (index >= questions.size()) {
            int totalPoints = pointsList.stream().mapToInt(Integer::intValue).sum();
            int earnedPoints = totalScore.get(chatId);

            // Очищаем данные пользователя
            currentTests.remove(chatId);
            currentIndexes.remove(chatId);
            correctAnswers.remove(chatId);
            totalScore.remove(chatId);
            questionPoints.remove(chatId);

            String languageLevel;
            if (earnedPoints <= 6) {
                languageLevel = "A1-A2 (Начальный)";
            } else if (earnedPoints <= 12) {
                languageLevel = "B1-B2 (Средний)";
            } else {
                languageLevel = "C1-C2 (Продвинутый)";
            }

            return "Тест завершён!\n\n" +
                    "Вы набрали " + earnedPoints + " баллов из " + totalPoints + " возможных.\n" +
                    "Ваш уровень владения языком " + languageLevel + "\n\n" +
                    "Отличная работа!";
        }

        currentIndexes.put(chatId, index);
        return questions.get(index);
    }

    /**
     * Проверяет, активен ли тест для пользователя.
     *
     * @param chatId
     * @return
     */

    public boolean isTestActive(long chatId) {
        return currentTests.containsKey(chatId) &&
                currentIndexes.containsKey(chatId) &&
                currentIndexes.get(chatId) < currentTests.get(chatId).size();
    }

    /**
     * Создаёт inline-клавиатуру для ответов на вопросы A/B/C/D.
     *
     * @return
     */

    public InlineKeyboardMarkup createAnswerKeyboard() {
        InlineKeyboardButton a = InlineKeyboardButton.builder()
                .text("A")
                .callbackData("A_button")
                .build();
        InlineKeyboardButton b = InlineKeyboardButton.builder()
                .text("B")
                .callbackData("B_button")
                .build();
        InlineKeyboardButton c = InlineKeyboardButton.builder()
                .text("C")
                .callbackData("C_button")
                .build();
        InlineKeyboardButton d = InlineKeyboardButton.builder()
                .text("D")
                .callbackData("D_button")
                .build();

        List<List<InlineKeyboardButton>> keyboard = List.of(List.of(a, b, c, d));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }
}
