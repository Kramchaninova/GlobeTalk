package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**StartBot.java - класс который обрабатывает команнду /start,
 * а именно: высылает создает приветсвенное письмо и кнопки под ним,
 * ну и соответсвенно реакции на эти кнопки
 */

public class StartBot {

    // Храним тесты, индексы и результаты для каждого пользователя
    private static final Map<Long, List<SendMessage>> currentTests = new HashMap<>();
    private static final Map<Long, Integer> currentIndexes = new HashMap<>();
    private static final Map<Long, List<String>> correctAnswers = new HashMap<>();
    private static final Map<Long, Integer> scoreMap = new HashMap<>();

    /**
     * startTest - метод привествия, те после нажания команды /start сдоровается и высылает кнопками варианты ответов
     * @param chatId
     * @return
     */
    public SendMessage startTest(long chatId) {

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Вас приветствует телеграмм бот GlobeTalk для изучения иностранных языков!\n\n" +
                        "Перед началом обучения, пройдите короткий тестик для определения вашего уровня владения языка.\n\n" +
                        "Для списка команд нажмите /help.\n\n"+
                        "Вы готовы начать тест?\n\n")
                .build();

        //уже создаем непосредственно кнопки
        // тоже нет конструктора надо делать как выше
        InlineKeyboardButton yes_button = InlineKeyboardButton.builder()
                .text("Конечно!")
                .callbackData("yes_button") // индификатор которы йпоказывает которая кнопка была нажата, она имеет тип стринг, позже пригодиться
                .build();

        InlineKeyboardButton no_button = InlineKeyboardButton.builder()
                .text("Совневаюсь...")
                .callbackData("no_button")
                .build();

        //список где кнопки хранятся грубо говоря в строку
        List<InlineKeyboardButton> rowInline  = new ArrayList<>();

        //добавляем уже кнопки ряд
        rowInline.add(yes_button); //эта будет слева
        rowInline.add(no_button); //эта будет справа

        //список списков то же самое что и в строку только еще на столбцы разделили
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(rowInline);

        // создаём разметку клавиатуры и добавляем в сообщение
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;

    }

    /**
     * HandleButtonClick - метод обрабатывающий реакции, взятые с кнопок
     * @param callbackData
     * @param chatId
     * @return
     */
    public SendMessage HandleButtonClick(String callbackData, long chatId){
        switch (callbackData){
            case "yes_button": {
                //ВНИАМНИЕ: тут класс создания и генерирования ответов

                //создаем экземпляр класса StartYesButton
                StartYesButton testGeneration = new StartYesButton();
                // генерация теста и возвращение его
                String test = testGeneration.getGeneratedTest();

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
                            .text(number + " (" + points + " points)\n\n" +
                                    question + "\n\n" +
                                    answerA + "\n" +
                                    answerB + "\n" +
                                    answerC + "\n" +
                                    answerD)
                            .build();

                    message.setReplyMarkup(createAnswerKeyboard());
                    questions.add(message);
                    answers.add(correctAnswer);
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

                return questions.get(0);
            }

            // Ответ пользователя (A,B,C или D)
            case "A_button":
            case "B_button":
            case "C_button":
            case "D_button": {
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
                int pointsForQuestion = 1; // по умолчанию
                if (pointMatcher.find()) {
                    pointsForQuestion = Integer.parseInt(pointMatcher.group(1));
                }

                // Проверяем ответ
                if (correct.get(index).equalsIgnoreCase(chosen)) {
                    score += pointsForQuestion;  // прибавляем реальные баллы
                    scoreMap.put(chatId, score);
                }

                // Следующий вопрос
                index++;
                List<SendMessage> questions = currentTests.get(chatId);

                if (index >= questions.size()) {
                    // Подсчёт общей суммы баллов
                    int totalPoints = 0;
                    Pattern totalPointPattern = Pattern.compile("\\((\\d+)\\s*p?o?i?n?t?s?\\)");
                    for (SendMessage q : questions) {
                        Matcher m = totalPointPattern.matcher(q.getText());
                        if (m.find()) {
                            totalPoints += Integer.parseInt(m.group(1));
                        }
                    }

                    int earnedPoints = scoreMap.get(chatId);

                    // Очищаем данные после завершения
                    currentTests.remove(chatId);
                    currentIndexes.remove(chatId);
                    correctAnswers.remove(chatId);
                    scoreMap.remove(chatId);

                    return SendMessage.builder()
                            .chatId(chatId)
                            .text("Тест завершён!\n\n" +
                                    "Вы набрали " + earnedPoints + " баллов из " + totalPoints + " возможных.\n\n" +
                                    "Отличная работа!")
                            .build();
                }

                currentIndexes.put(chatId, index);
                return questions.get(index);
            }



            case "no_button":
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Не сомневайтесь в себе!!!\n\n"+
                                "Когда будете готовы используйте /start.\n\n"+
                                "Для списка команд нажмите /help.\n\n")
                        .build();
            default:
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Неизвестная команда")
                        .build();
        }
    }

    /**
     * Клавиатура с ответами A-D
     */

    private InlineKeyboardMarkup createAnswerKeyboard() {
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
