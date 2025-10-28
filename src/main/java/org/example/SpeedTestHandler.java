package org.example;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Обработка логики speed теста:
 * Хранит вопросы, ответы, текущий индекс, таймеры и количество набранных баллов для каждого пользователя.
 * Отвечает за генерацию теста из текстовой строки, обработку ответов пользователей
 * подсчёт баллов, создание inline-клавиатуры для ответов A/B/C/D.
 *
 *
 */
public class SpeedTestHandler {

    // храним данные для каждого пользователя
    private final Map<Long, List<String>> currentTests = new HashMap<>();
    private final Map<Long, List<String>> correctAnswers = new HashMap<>();
    private final Map<Long, Integer> currentIndexes = new HashMap<>();
    private final Map<Long, Integer> totalScore = new HashMap<>();
    private final Map<Long, List<Integer>> questionPoints = new HashMap<>();

    // таймеры для каждого пользователя
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> userTimers = new ConcurrentHashMap<>();
    //ScheduledExecutorService - планировщик задач, который выполняет код через время
    private static final ScheduledExecutorService TIMER_POOL =
            //создание потоков планировщиков
            Executors.newScheduledThreadPool(20);

    // констатны ответов на задач в секундах
    private static final int TIME_FOR_1_POINT = 5;
    private static final int TIME_FOR_2_POINTS = 10;
    private static final int TIME_FOR_3_POINTS = 20;

    private static final String ANSWER_ERROR = "Не удалось распознать вопросы в тесте.";
    private static final String AGAIN_TEST = "Сначала начните тест командой /speed_test.";

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

        currentTests.put(chatId, questions);
        correctAnswers.put(chatId, answers);
        currentIndexes.put(chatId, 0);
        totalScore.put(chatId, 0);
        questionPoints.put(chatId, pointsList);

        startQuestionTimer(chatId);

        return formatQuestionWithTimer(questions.getFirst(), getCurrentQuestionPoints(chatId));
    }

    /**
     * формирует сообщение с вопросом + информирует о времени на вопрос
     */
    private String formatQuestionWithTimer(String question, int points) {
        int timeLimit = getTimeForPoints(points);
        return question + "\n\nВремя на ответ: " + timeLimit + " секунд";
    }

    /**
     * получает время для вопроса в зависимости от баллов (points)
     */
    private int getTimeForPoints(int points) {
        switch (points) {
            case 1: return TIME_FOR_1_POINT;
            case 2: return TIME_FOR_2_POINTS;
            case 3: return TIME_FOR_3_POINTS;
            default: return TIME_FOR_1_POINT;
        }
    }

    /**
     * Обрабатывает ответ от пользовтаеля, с обратной связью
     */
    public Map<String, Object> handleAnswerWithFeedback(String callbackData, long chatId) {
        Map<String, Object> result = new HashMap<>();

        if (!isTestActive(chatId)) {
            result.put("feedback", AGAIN_TEST);
            result.put("isCorrect", false);
            return result;
        }
        //получает таймер и смотрит не истекло ли
        ScheduledFuture<?> timer = userTimers.get(chatId);
        if (timer != null && timer.isDone() && !timer.isCancelled()) {
            // если время истекло - удаляем таймер и возвращаем сообщение
            userTimers.remove(chatId);
            result.put("feedback", "Время вышло! Ответ не засчитан.");
            result.put("isCorrect", false);
            return result;
        }

        //остановка таймера (пользователь ответи)
        stopTimer(chatId);
        //извлекаем выбранную кнопку
        String chosen = callbackData.substring(0, 1);
        //получаем данные пользоватля
        int index = currentIndexes.get(chatId);//текущий номер вопсроа
        List<String> correct = correctAnswers.get(chatId); //правильный ответ
        List<Integer> pointsList = questionPoints.get(chatId); //баллы

        //сравниваем ответы
        String correctAnswer = correct.get(index);
        boolean isCorrect = correctAnswer.equalsIgnoreCase(chosen);

        String feedback;
        if (isCorrect) {
            int score = totalScore.get(chatId);
            score += pointsList.get(index);
            totalScore.put(chatId, score);
            feedback = "Правильно!";
        } else {
            feedback = "Вы ошиблись, правильный ответ: " + correctAnswer;
        }

        //результат возврата
        result.put("feedback", feedback);
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", correctAnswer);
        result.put("currentQuestionPoints", pointsList.get(index));

        return result;
    }

    /**
     * Переход к следующему вопросу
     */
    public String moveToNextQuestion(long chatId) {
        if (!isTestActive(chatId)) {
            return AGAIN_TEST;
        }
        //обновляем индекс вопроса
        int index = currentIndexes.get(chatId);
        index++;
        currentIndexes.put(chatId, index);

        List<String> questions = currentTests.get(chatId);
        //проверка на конец теста
        if (index >= questions.size()) {
            return finishTest(chatId);
        }
        //запускаем таймер для нового вопроса
        startQuestionTimer(chatId);
        //возвращаем следующий вопрос для пользователя
        return formatQuestionWithTimer(questions.get(index), getCurrentQuestionPoints(chatId));
    }

    /**
     * Обрабатывает истечение времени
     */
    public Map<String, Object> handleTimeExpired(long chatId) {
        Map<String, Object> result = new HashMap<>();

        if (!isTestActive(chatId)) {
            result.put("feedback", AGAIN_TEST);
            result.put("correctAnswer", "");
            return result;
        }

        //получаем правильный ответ и уведомляем что время вышло
        String correctAnswer = getCurrentCorrectAnswer(chatId);
        String feedback = "Время вышло! Правильный ответ: " + correctAnswer;

        //заполняем результаты для возврата
        result.put("feedback", feedback);
        result.put("correctAnswer", correctAnswer);
        result.put("currentQuestionPoints", getCurrentQuestionPoints(chatId));

        return result;
    }

    /**
     * запускает таймер для текущего вопроса
     */
    public void startQuestionTimer(long chatId) {
        if (!isTestActive(chatId)) {
            return;
        }
        //определяет тайм лимит
        int timeLimit = getTimeForPoints(getCurrentQuestionPoints(chatId));
        //останавливаем предыдущий, чтобы избежать наложения
        stopTimer(chatId);

        //создаем новый таймер в общем потоке
        ScheduledFuture<?> timer = TIMER_POOL.schedule(() -> {
            handleTimeExpired(chatId);
        }, timeLimit, TimeUnit.SECONDS);
        //сохраняем ссылку на таймер, чтобы если что его оставновить
        userTimers.put(chatId, timer);
    }

    /**
     * Останавка таймер
     */
    public void stopTimer(long chatId) {
        //достаем таймер из мапа и одновременно удаляем его
        ScheduledFuture<?> timer = userTimers.remove(chatId);
        if (timer != null) {
            timer.cancel(false);
        }
    }

    /**
     * получает правильный ответ для текущего вопроса
     */
    public String getCurrentCorrectAnswer(long chatId) {
        if (!isTestActive(chatId)) {
            return null;
        }

        int index = currentIndexes.get(chatId);
        List<String> correct = correctAnswers.get(chatId);
        return correct.get(index);
    }

    /**
     * получает баллы за текущий вопрос
     */
    public int getCurrentQuestionPoints(long chatId) {
        if (!isTestActive(chatId)) {
            return 0;
        }

        int index = currentIndexes.get(chatId);
        List<Integer> pointsList = questionPoints.get(chatId);
        return pointsList.get(index);
    }

    /**
     * Завершает тест и возвращает результат
     */
    private String finishTest(long chatId) {
        List<Integer> pointsList = questionPoints.get(chatId);
        //считыем макс возможное колво ьаллов
        int totalPoints = pointsList.stream().mapToInt(Integer::intValue).sum();
        int earnedPoints = totalScore.get(chatId); //фактическое

        cleanupTestData(chatId);
        //делаем фиальное сообщение
        return generateSpeedTestResult(earnedPoints, totalPoints);
    }

    /**
     *  результат для speed test
     */
    private String generateSpeedTestResult(int earnedPoints, int totalPoints) {
        String performanceMessage;

        if (earnedPoints >= 18 && earnedPoints <= 24) {
            performanceMessage = "Отличный результат!";
        } else if (earnedPoints >= 12 && earnedPoints <= 17) {
            performanceMessage = "Хороший результат!";
        } else {
            performanceMessage = "Стоит поработать!";
        }

        double percentage = totalPoints > 0 ? (double) earnedPoints / totalPoints * 100 : 0;

        return performanceMessage + "\n\n" +
                "Вы набрали " + earnedPoints + " баллов из " + totalPoints + " возможных.\n" +
                "Процент выполнения: " + String.format("%.1f", percentage) + "%";
    }

    /**
     * очищает все данные теста
     */
    private void cleanupTestData(long chatId) {
        stopTimer(chatId);
        currentTests.remove(chatId);
        currentIndexes.remove(chatId);
        correctAnswers.remove(chatId);
        totalScore.remove(chatId);
        questionPoints.remove(chatId);
    }

    /**
     * проверка на активность теста
     */
    public boolean isTestActive(long chatId) {
        return currentTests.containsKey(chatId) &&
                currentIndexes.containsKey(chatId) &&
                currentIndexes.get(chatId) < currentTests.get(chatId).size();
    }
}