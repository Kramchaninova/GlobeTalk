package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestSpeedTest {
    SpeedTestHandler speedTestHandler = new SpeedTestHandler();

    private String testText = """
            1 (1 points)
            Question 1?
            A. Wrong
            B. Correct
            C. Wrong
            D. Wrong
            Answer: B
            
            2 (2 points)
            Question 2?
            A. Wrong
            B. Correct
            C. Wrong
            D. Wrong
            Answer: B
            
            3 (3 points)
            Question 3?
            A. Wrong
            B. Correct
            C. Wrong
            D. Wrong
            Answer: B
            """;

    /**
     * Тест обработки правильного ответа в speed тесте
     */
    @Test
    void testSpeedTestCorrectAnswer() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(99999L, testText);

        Map<String, Object> result = speedTestHandler.handleAnswerWithFeedback("B", 99999L);

        Assertions.assertEquals("Правильно!", result.get("feedback"));
        Assertions.assertEquals(true, result.get("isCorrect"));
        Assertions.assertEquals("B", result.get("correctAnswer"));
    }

    /**
     * Тест обработки неправильного ответа в speed тесте
     */
    @Test
    void testSpeedTestIncorrectAnswer() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(99998L, testText);

        Map<String, Object> result = speedTestHandler.handleAnswerWithFeedback("A", 99998L);

        Assertions.assertEquals("Вы ошиблись, правильный ответ: B", result.get("feedback"));
        Assertions.assertEquals(false, result.get("isCorrect"));
        Assertions.assertEquals("B", result.get("correctAnswer"));
    }

    /**
     * Тест времени для разных вопросов по баллам
     */
    @Test
    void testQuestionTimeLimits() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(10002L, testText);

        // проверяем первый вопрос (1 балл = 5 секунд)
        int points1 = speedTestHandler.getCurrentQuestionPoints(10002L);
        Assertions.assertEquals(1, points1);

        // отвечаем и переходим ко второму вопросу (2 балла = 10 секунд)
        speedTestHandler.handleAnswerWithFeedback("B", 10002L);
        speedTestHandler.moveToNextQuestion(10002L);
        int points2 = speedTestHandler.getCurrentQuestionPoints(10002L);
        Assertions.assertEquals(2, points2);

        // отвечаем и переходим к третьему вопросу (3 балла = 20 секунд)
        speedTestHandler.handleAnswerWithFeedback("B", 10002L);
        speedTestHandler.moveToNextQuestion(10002L);
        int points3 = speedTestHandler.getCurrentQuestionPoints(10002L);
        Assertions.assertEquals(3, points3);
    }

    /**
     * Тест на низкий результат
     * Самое главное в тесте это первая строка вывода, потому что от баллов зависит текст
     */
    @Test
    void testVeryLowResult() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();

        String lowScoreTest = """
                1 (3 points)
                Question 1?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                2 (3 points)
                Question 2?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                3 (3 points)
                Question 3?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                4 (3 points)
                Question 4?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        speedTestHandler.generateTest(20009L, lowScoreTest);

        // только 1 правильный ответ из 4 (3 балла из 12 = 25%)
        speedTestHandler.handleAnswerWithFeedback("B", 20009L); // +3
        speedTestHandler.moveToNextQuestion(20009L);
        speedTestHandler.handleAnswerWithFeedback("A", 20009L); // +0
        speedTestHandler.moveToNextQuestion(20009L);
        speedTestHandler.handleAnswerWithFeedback("A", 20009L); // +0
        speedTestHandler.moveToNextQuestion(20009L);
        speedTestHandler.handleAnswerWithFeedback("A", 20009L); // +0

        String finalMessage = speedTestHandler.moveToNextQuestion(20009L);

        String expectedMessage = "💪 *Есть над чем поработать!* 💪\n\n" +
                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 3 из 12 возможных\n" +
                "📈 Процент выполнения: 25,0%\n\n" +
                "✨ **Продолжайте в том же духе!** ✨\n\n" +
                "Для продолжения работы используйте команды:\n" +
                "• /start - пройти тест заново\n" +
                "• /speed_test - тест на скорость\n" +
                "• /help - все доступные команды\n\n";

        Assertions.assertEquals(expectedMessage, finalMessage);
    }

    /**
     * Тест итогового сообщения со средним результатом (граница между категориями)
     * Самое главное в тесте это первая строка вывода, потому что от баллов зависит текст
     */
    @Test
    void testFinalMessageAverageResult() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();

        String testForAverage = """
                1 (2 points)
                Question 1?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                2 (2 points)
                Question 2?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                3 (2 points)
                Question 3?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                4 (2 points)
                Question 4?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                5 (2 points)
                Question 5?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                6 (2 points)
                Question 6?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        speedTestHandler.generateTest(20004L, testForAverage);

        // все ответы правильные (12 баллов из 12)
        speedTestHandler.handleAnswerWithFeedback("B", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B", 20004L); // +2

        String finalMessage = speedTestHandler.moveToNextQuestion(20004L);

        String expectedMessage = "👍 *Хороший результат!* 👍\n\n" +
                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 12 из 12 возможных\n" +
                "📈 Процент выполнения: 100,0%\n\n" +
                "✨ **Продолжайте в том же духе!** ✨\n\n" +
                "Для продолжения работы используйте команды:\n" +
                "• /start - пройти тест заново\n" +
                "• /speed_test - тест на скорость\n" +
                "• /help - все доступные команды\n\n";

        Assertions.assertEquals(expectedMessage, finalMessage);
    }

    /**
     * Тест итогового сообщения с результатом на границе хорошего и отличного
     * Самое главное в тесте это первая строка вывода, потому что от баллов зависит текст
     */
    @Test
    void testFinalMessageGoodToExcellentBoundary() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();

        String boundaryTest = """
                1 (3 points)
                Question 1?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                2 (3 points)
                Question 2?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                3 (3 points)
                Question 3?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                4 (3 points)
                Question 4?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                5 (3 points)
                Question 5?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                6 (3 points)
                Question 6?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        speedTestHandler.generateTest(20005L, boundaryTest);

        // все ответы правильные (18 баллов из 18)
        speedTestHandler.handleAnswerWithFeedback("B", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B", 20005L); // +3

        String finalMessage = speedTestHandler.moveToNextQuestion(20005L);

        String expectedMessage = "🎉 *Отличный результат!* 🎉\n\n" +
                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 18 из 18 возможных\n" +
                "📈 Процент выполнения: 100,0%\n\n" +
                "✨ **Продолжайте в том же духе!** ✨\n\n" +
                "Для продолжения работы используйте команды:\n" +
                "• /start - пройти тест заново\n" +
                "• /speed_test - тест на скорость\n" +
                "• /help - все доступные команды\n\n";

        Assertions.assertEquals(expectedMessage, finalMessage);
    }

    /**
     * Тест обработки истечения времени
     */
    @Test
    void testTimeExpired() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(30001L, testText);

        Map<String, Object> result = speedTestHandler.handleTimeExpired(30001L);

        Assertions.assertEquals("Время вышло! Правильный ответ: B", result.get("feedback"));
        Assertions.assertEquals("B", result.get("correctAnswer"));
        Assertions.assertEquals(1, result.get("currentQuestionPoints"));
    }


    /**
     * Тест проверки активности теста
     */
    @Test
    void testIsTestActive() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();

        // До начала теста
        Assertions.assertEquals(false, speedTestHandler.isTestActive(30003L));

        // После начала теста
        speedTestHandler.generateTest(30003L, testText);
        Assertions.assertEquals(true, speedTestHandler.isTestActive(30003L));

        // После завершения теста
        speedTestHandler.handleAnswerWithFeedback("B", 30003L);
        speedTestHandler.moveToNextQuestion(30003L);
        speedTestHandler.handleAnswerWithFeedback("B", 30003L);
        speedTestHandler.moveToNextQuestion(30003L);
        speedTestHandler.handleAnswerWithFeedback("B", 30003L);
        speedTestHandler.moveToNextQuestion(30003L);
        Assertions.assertEquals(false, speedTestHandler.isTestActive(30003L));
    }

    /**
     * Тест времени для разных баллов через проверку формата вопроса
     */
    @Test
    void testQuestionTimeFormat() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        String firstQuestion = speedTestHandler.generateTest(30004L, testText);

        // Первый вопрос - 1 балл = 5 секунд
        String expectedTime1 = "Время на ответ: 5 секунд";
        Assertions.assertEquals(expectedTime1, firstQuestion.substring(firstQuestion.indexOf("Время на ответ:")));

        // Второй вопрос - 2 балла = 10 секунд
        speedTestHandler.handleAnswerWithFeedback("B", 30004L);
        String question2 = speedTestHandler.moveToNextQuestion(30004L);
        String expectedTime2 = "Время на ответ: 10 секунд";
        Assertions.assertEquals(expectedTime2, question2.substring(question2.indexOf("Время на ответ:")));

        // Третий вопрос - 3 балла = 20 секунд
        speedTestHandler.handleAnswerWithFeedback("B", 30004L);
        String question3 = speedTestHandler.moveToNextQuestion(30004L);
        String expectedTime3 = "Время на ответ: 20 секунд";
        Assertions.assertEquals(expectedTime3, question3.substring(question3.indexOf("Время на ответ:")));
    }

    /**
     * Тест очистки данных после завершения теста
     */
    @Test
    void testCleanupAfterTest() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(30005L, testText);

        // Проверяем, что данные есть до завершения
        Assertions.assertEquals(true, speedTestHandler.isTestActive(30005L));
        Assertions.assertEquals("B", speedTestHandler.getCurrentCorrectAnswer(30005L));
        Assertions.assertEquals(1, speedTestHandler.getCurrentQuestionPoints(30005L));

        // Завершаем тест
        speedTestHandler.handleAnswerWithFeedback("B", 30005L);
        speedTestHandler.moveToNextQuestion(30005L);
        speedTestHandler.handleAnswerWithFeedback("B", 30005L);
        speedTestHandler.moveToNextQuestion(30005L);
        speedTestHandler.handleAnswerWithFeedback("B", 30005L);
        speedTestHandler.moveToNextQuestion(30005L);

        // Проверяем, что все данные очищены
        Assertions.assertEquals(false, speedTestHandler.isTestActive(30005L));
        Assertions.assertEquals(null, speedTestHandler.getCurrentCorrectAnswer(30005L));
        Assertions.assertEquals(0, speedTestHandler.getCurrentQuestionPoints(30005L));
    }

    /**
     * Тест получения правильного ответа для текущего вопроса
     */
    @Test
    void testGetCurrentCorrectAnswer() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(30006L, testText);

        // Первый вопрос - ответ B
        Assertions.assertEquals("B", speedTestHandler.getCurrentCorrectAnswer(30006L));

        // Переходим ко второму вопросу - ответ B
        speedTestHandler.handleAnswerWithFeedback("B", 30006L);
        speedTestHandler.moveToNextQuestion(30006L);
        Assertions.assertEquals("B", speedTestHandler.getCurrentCorrectAnswer(30006L));
    }

    /**
     * Тест обработки истечения времени
     */
    @Test
    void testHandleTimeExpired() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(30007L, testText);

        Map<String, Object> result = speedTestHandler.handleTimeExpired(30007L);

        Assertions.assertEquals("Время вышло! Правильный ответ: B", result.get("feedback"));
        Assertions.assertEquals("B", result.get("correctAnswer"));
        Assertions.assertEquals(1, result.get("currentQuestionPoints"));
    }

    /**
     * Тест обработки неактивного теста
     */
    @Test
    void testInactiveTestHandling() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();

        // Попытка обработать ответ без активного теста
        Map<String, Object> answerResult = speedTestHandler.handleAnswerWithFeedback("B", 30008L);
        Assertions.assertEquals("Сначала начните тест командой /speed_test.", answerResult.get("feedback"));
        Assertions.assertEquals(false, answerResult.get("isCorrect"));

        // Попытка перейти к следующему вопросу без активного теста
        String nextQuestion = speedTestHandler.moveToNextQuestion(30008L);
        Assertions.assertEquals("Сначала начните тест командой /speed_test.", nextQuestion);

        // Попытка обработать истечение времени без активного теста
        Map<String, Object> timeResult = speedTestHandler.handleTimeExpired(30008L);
        Assertions.assertEquals("Сначала начните тест командой /speed_test.", timeResult.get("feedback"));
        Assertions.assertEquals("", timeResult.get("correctAnswer"));
    }

    /**
     * Тест повторного использования chatId после завершения теста
     */
    @Test
    void testReuseChatIdAfterTest() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();

        // Первый тест
        speedTestHandler.generateTest(30009L, testText);
        Assertions.assertEquals(true, speedTestHandler.isTestActive(30009L));

        // Завершаем первый тест
        speedTestHandler.handleAnswerWithFeedback("B", 30009L);
        speedTestHandler.moveToNextQuestion(30009L);
        speedTestHandler.handleAnswerWithFeedback("B", 30009L);
        speedTestHandler.moveToNextQuestion(30009L);
        speedTestHandler.handleAnswerWithFeedback("B", 30009L);
        String result = speedTestHandler.moveToNextQuestion(30009L);
        Assertions.assertEquals(false, speedTestHandler.isTestActive(30009L));

        // Запускаем новый тест с тем же chatId
        String newTest = speedTestHandler.generateTest(30009L, testText);
        Assertions.assertEquals(true, speedTestHandler.isTestActive(30009L));
        Assertions.assertEquals(true, newTest.contains("Question 1?"));
    }

    /**
     * Тест баллов за вопросы
     */
    @Test
    void testQuestionPoints() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(30010L, testText);

        // Первый вопрос - 1 балл
        Assertions.assertEquals(1, speedTestHandler.getCurrentQuestionPoints(30010L));

        // Второй вопрос - 2 балла
        speedTestHandler.handleAnswerWithFeedback("B", 30010L);
        speedTestHandler.moveToNextQuestion(30010L);
        Assertions.assertEquals(2, speedTestHandler.getCurrentQuestionPoints(30010L));

        // Третий вопрос - 3 балла
        speedTestHandler.handleAnswerWithFeedback("B", 30010L);
        speedTestHandler.moveToNextQuestion(30010L);
        Assertions.assertEquals(3, speedTestHandler.getCurrentQuestionPoints(30010L));
    }

    /**
     * Тест начисления баллов за правильные ответы
     */
    @Test
    void testScoreCalculation() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(30011L, testText);

        // Первый ответ правильный +1 балл
        Map<String, Object> result1 = speedTestHandler.handleAnswerWithFeedback("B", 30011L);
        Assertions.assertEquals(true, result1.get("isCorrect"));

        // Переход ко второму вопросу
        speedTestHandler.moveToNextQuestion(30011L);

        // Второй ответ правильный +2 балла
        Map<String, Object> result2 = speedTestHandler.handleAnswerWithFeedback("B", 30011L);
        Assertions.assertEquals(true, result2.get("isCorrect"));

        // Переход к третьему вопросу
        speedTestHandler.moveToNextQuestion(30011L);

        // Третий ответ правильный +3 балла
        Map<String, Object> result3 = speedTestHandler.handleAnswerWithFeedback("B", 30011L);
        Assertions.assertEquals(true, result3.get("isCorrect"));

        // Завершение теста - проверяем итоговый счет (1+2+3=6 баллов)
        String finalMessage = speedTestHandler.moveToNextQuestion(30011L);
        Assertions.assertEquals(true, finalMessage.contains("Набрано баллов: 6 из 6 возможных"));
    }
}