package org.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TestManager.java — класс для обработки и хранения логики теста:
 *  - парсинг текста теста;
 *  - создание сообщений с вопросами и кнопками;
 *  - подсчёт баллов и переход между вопросами.
 */
public class TestManager {

    // Храним как строки тесты, ответы, индексы и баллы для каждого пользователя
    private final Map<Long, List<String>> currentTests = new HashMap<>();
    private final Map<Long, List<String>> correctAnswers = new HashMap<>();
    private final Map<Long, Integer> currentIndexes = new HashMap<>();
    private final Map<Long, Integer> totalScore = new HashMap<>();
    private final Map<Long, List<Integer>> questionPoints = new HashMap<>(); //и храним поинтсы на каждый вопрос

    private static final String ANSWER_ERROR = "Не удалось распознать вопросы в тесте.";
    private static final String AGAIN_TEST= "Сначала начните тест командой /start.";
    /**
     * Метод, который разбирает текст с тестом, извлекает вопросы, варианты ответов и правильные ответы.
     * Сохраняет вопросы и ответы в отдельные структуры данных для конкретного пользователя.
     * Инициализирует индекс текущего вопроса и общий счётчик баллов.
     * Возвращает текст первого вопроса для начала теста.
     */
    public String generateTest(long chatId, String test) {
        Pattern pattern = Pattern.compile(
                "(\\d+)\\s*\\(?(\\d+)\\)?\\s*\\n" +       // номер и баллы: "1 (1)"
                        "(.+?)\\n" +                             // текст вопроса
                        "(A\\..+?)\\n" +                         // вариант A
                        "(B\\..+?)\\n" +                         // вариант B
                        "(C\\..+?)\\n" +                         // вариант C
                        "(D\\..+?)\\n" +                         // вариант D
                        "Answer:\\s*([A-D])",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(test);
        List<String> questions = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        List<Integer> pointsList = new ArrayList<>();
        int foundQuestions = 0;
        // Обрабатываем каждый вопрос из сгенерированного теста:
        // извлекаем номер, баллы, текст вопроса, варианты ответов и правильный ответ.
        while (matcher.find()) {
            foundQuestions++;
            try {
                String number = matcher.group(1);
                String points = matcher.group(2);
                String question = matcher.group(3);
                String answerA = matcher.group(4);
                String answerB = matcher.group(5);
                String answerC = matcher.group(6);
                String answerD = matcher.group(7);
                String correctAnswer = matcher.group(8).trim();

                String questionText =question + "\n\n" +
                        answerA + "\n" +
                        answerB + "\n" +
                        answerC + "\n" +
                        answerD;

                questions.add(questionText);
                answers.add(correctAnswer);
                pointsList.add(Integer.parseInt(points));
                System.out.println(questionText+ "\n");
            } catch (Exception e) {
                System.out.println("Ошибка при обработке вопроса " + foundQuestions + ": " + e.getMessage());
            }
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
     *
     * Обрабатывает выбор ответа пользователя на вопрос теста (кнопки A/B/C/D).
     * Проверяет правильность ответа, начисляет баллы и возвращает следующий вопрос.
     * Если вопросы закончились, подсчитывает итоговый результат и уровень владения языком.
     * @param callbackData
     * @param chatId
     * @return
     */
    public String handleAnswer(String callbackData, long chatId) {
        if (!currentTests.containsKey(chatId)) {
            return AGAIN_TEST;
        }

        // Определяем выбранную букву
        String chosen = callbackData.substring(0, 1);
        int index = currentIndexes.get(chatId);
        List<String> correct = correctAnswers.get(chatId);
        int score = totalScore.get(chatId);
        int pointsForQuestion = questionPoints.get(chatId).get(index);

        // Проверяем ответ
        if (correct.get(index).equalsIgnoreCase(chosen)) {
            score += pointsForQuestion;
            totalScore.put(chatId, score);
        }

        // Переход к следующему вопросу
        index++;
        List<String> questions = currentTests.get(chatId);

        if (index >= questions.size()) {
            // Подсчёт общей суммы баллов
            int totalPoints = questionPoints.get(chatId).stream().mapToInt(Integer::intValue).sum();


            int earnedPoints = totalScore.get(chatId);

            // Очищаем данные после завершения
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



    public boolean isTestActive(long chatId) {
        return currentTests.containsKey(chatId) &&
                currentIndexes.containsKey(chatId) &&
                currentIndexes.get(chatId) < currentTests.get(chatId).size();
    }
}
