package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TestManager.java — класс для обработки и хранения логики теста:
 * - парсинг текста теста;
 * - создание сообщений с вопросами и кнопками;
 * - подсчёт баллов и переход между вопросами.
 */
public class TestManager {

    // Храним тесты, ответы, индексы, баллы, общее кол-во баллов для каждого пользователя
    private static final Map<Long, List<SendMessage>> currentTests = new HashMap<>();
    private static final Map<Long, List<String>> correctAnswers = new HashMap<>();
    private static final Map<Long, Integer> currentIndexes = new HashMap<>();
    private static final Map<Long, Integer> scoreMap = new HashMap<>();
    private static final Map<Long, List<Integer>> questionPoints = new HashMap<>();

    /**
     * Генерация списка вопросов и правильных ответов из текста теста
     */
    public static SendMessage generateTest(long chatId, String test) {
        Pattern pattern = Pattern.compile(
                "(\\d+)\\s*\\((\\d+)\\s*p?o?i?n?t?s?\\)\\s*\\n" +
                        "(.+?)\\n" +
                        "(A\\..+?)\\n" +
                        "(B\\..+?)\\n" +
                        "(C\\..+?)\\n" +
                        "(D\\..+?)\\n" +
                        "Answer:\\s+?([A-D])",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(test);
        List<SendMessage> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        List<Integer> pointsList = new ArrayList<>();

        // Обрабатываем каждый вопрос из сгенерированного теста:
        // извлекаем номер, баллы, текст вопроса, варианты ответов и правильный ответ.
        while (matcher.find()) {
            String number = matcher.group(1);
            String points = matcher.group(2);
            String question = matcher.group(3);
            String answerA = matcher.group(4);
            String answerB = matcher.group(5);
            String answerC = matcher.group(6);
            String answerD = matcher.group(7);
            String correctAnswer = matcher.group(8).trim();

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(question + "\n\n" +
                            answerA + "\n" +
                            answerB + "\n" +
                            answerC + "\n" +
                            answerD)
                    .build();

            message.setReplyMarkup(createAnswerKeyboard());
            questions.add(message);
            answers.add(correctAnswer);
            pointsList.add(Integer.parseInt(points));
        }

        if (questions.isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Не удалось распознать вопросы в тесте.")
                    .build();
        }

        currentTests.put(chatId, questions);
        correctAnswers.put(chatId, answers);
        currentIndexes.put(chatId, 0);
        scoreMap.put(chatId, 0);
        questionPoints.put(chatId, pointsList);

        return questions.get(0);
    }

    /**
     * Обработка нажатий на кнопки A/B/C/D
     */
    public static SendMessage handleAnswer(String callbackData, long chatId) {
        if (!currentTests.containsKey(chatId)) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Сначала начните тест командой /start.")
                    .build();
        }

        // Определяем выбранную букву
        String chosen = callbackData.substring(0, 1);
        int index = currentIndexes.get(chatId);
        List<String> correct = correctAnswers.get(chatId);
        int score = scoreMap.get(chatId);

        // Получаем количество баллов за этот вопрос из текста
        String questionText = currentTests.get(chatId).get(index).getText();
        Pattern pointPattern = Pattern.compile("\\((\\d+)\\s*p?o?i?n?t?s?\\)");
        Matcher pointMatcher = pointPattern.matcher(questionText);
        int pointsForQuestion = 1;
        if (pointMatcher.find()) {
            pointsForQuestion = Integer.parseInt(pointMatcher.group(1));
        }

        // Проверяем ответ
        if (correct.get(index).equalsIgnoreCase(chosen)) {
            score += pointsForQuestion;
            scoreMap.put(chatId, score);
        }

        // Переход к следующему вопросу
        index++;
        List<SendMessage> questions = currentTests.get(chatId);

        if (index >= questions.size()) {
            // Подсчёт общей суммы баллов
            int totalPoints = questionPoints.get(chatId).stream().mapToInt(Integer::intValue).sum();

            int earnedPoints = scoreMap.get(chatId);

            // Очищаем данные после завершения
            currentTests.remove(chatId);
            currentIndexes.remove(chatId);
            correctAnswers.remove(chatId);
            scoreMap.remove(chatId);
            questionPoints.remove(chatId);

            if (earnedPoints <= 6) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Тест завершён!\n\n" +
                                "Вы набрали " + earnedPoints + " баллов из " + totalPoints + " возможных.\n" +
                                "Вы уровень владения языком A1-A2 (Начальный).\n\n" +
                                "Отличная работа!")
                        .build();
            } else if (earnedPoints <= 12) {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Тест завершён!\n\n" +
                                "Вы набрали " + earnedPoints + " баллов из " + totalPoints + " возможных.\n" +
                                "Вы уровень владения языком B1-B2 (Средний).\n\n" +
                                "Отличная работа!")
                        .build();
            } else {
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Тест завершён!\n\n" +
                                "Вы уровень владения языком C1-C2 (Продвинутый).\n\n" +
                                "Вы набрали " + earnedPoints + " баллов из " + totalPoints + " возможных.\n" +
                                "Отличная работа!")
                        .build();
            }

        }

        currentIndexes.put(chatId, index);
        return questions.get(index);
    }

    /**
     * Клавиатура с ответами A-D
     */
    private static InlineKeyboardMarkup createAnswerKeyboard() {

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
