package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.Map;

/**
 * TelegramBotTest - тесты на логику и на наличие кнопок в списках
 */

public class TelegramBotTest {
    private final BotLogic botLogic = new BotLogic();
    private final TestHandler testHandler = new TestHandler();
    public final KeyboardService keyboardService = new KeyboardService();
    public final SpeedTestHandler speedTestHandler = new SpeedTestHandler();

    /**
     * Проверка команды /start
     */
    @Test
    void testStartCommand() {
        String result = botLogic.handleCommand("/start");

        Assertions.assertEquals("🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
                "Ваш персональный помощник в изучении иностранных языков! 🎯\n\n" +
                "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n" +
                "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
                "🔍 Для просмотра всех команд нажмите /help\n\n" +
                "🚀 **Вы готовы начать тест?**", result);
    }

    /**
     * Проверка команды /help
     */
    @Test
    void testHelpCommand() {
        String result = botLogic.handleCommand("/help");

        Assertions.assertEquals("🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

                "📋 **Доступные команды:**\n" +
                "• /start - Начать работу с ботом и пройти тестирование\n" +
                "• /help - Показать эту справку\n" +
                "• /speed_test - пройти тест на скорость\n\n" +

                "🎯 **Как работает бот:**\n" +
                "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
                "• 📝 Тестирование для определения вашего уровня\n" +
                "• 🎮 Интерактивные упражнения\n\n" +

                "🛠️ **В процессе разработки:****\n" +
                "• 📊 Отслеживание прогресса\n" +
                "• 📚Словарь и словарный запас**\n\n"+


                "💡 **Как взаимодействовать:**\n" +
                "• Используйте команды из меню (слева)\n" +
                "• Нажимайте на кнопки под сообщениями\n" +
                "• Отвечайте на вопросы теста\n" +
                "• Следите за своим прогрессом в профиле\n\n" +

                "🚀 **Начните с команды /start чтобы определить ваш уровень!**", result);
    }

    /**
     * Проверка неизвестной команды
     */
    @Test
    void testUnknownCommand() {
        String result = botLogic.handleCommand("/unknown");

        Assertions.assertEquals("Неизвестная команда. Введите /help для списка доступных команд.", result);
    }

    private String testText;

    /**
     * Проверка генерации теста для последующих тестов
     */
    @BeforeEach
    void testGenerateTest() {
        testText = """
                1 (1 points)
                What is the capital of France?
                A. London
                B. Paris
                C. Madrid
                D. Rome
                Answer: B
                
                2 (2 points)
                Which word is a verb?
                A. Apple
                B. Run
                C. Table
                D. House
                Answer: B
                
                3 (3 points)
                Choose the correct sentence:
                A. He go to school.
                B. He goes to school.
                C. He going to school.
                D. He gone to school.
                Answer: B
                """;

        testHandler.generateTest(12345L, testText);
    }

    /**
     * Проверка разницы между правильным и неправильным ответом
     * Она работает через TestHendler, те есть ответ от ии в виде вопроса и ответа
     */
    @Test
    void testCorrectVsIncorrectScoreDifference() {
        String simpleTest = """
            1 (10 points)
            Simple question?
            A. Wrong
            B. Correct
            C. Wrong
            D. Wrong
            Answer: B
            """;

        // ТЕСТ 1: правильный ответ
        testHandler.generateTest(11111L, simpleTest);
        //псевдопользователь нажимает на кнопку, в данном случае правильную
        //и в correctFinalResponse начисляются баллы
        String correctFinalResponse = testHandler.handleAnswer("B_button", 11111L);

        // ТЕСТ 2: неправильный ответ
        testHandler.generateTest(22222L, simpleTest);
        //тоже самое, но баллы не начиляются
        String incorrectFinalResponse = testHandler.handleAnswer("A_button", 22222L);

        // для правильного ответа ожидаем 10 баллов (указано в сообщении)
        //тут специально стоит проверка assertTrue, потому что мы проверяем подстроку, а именно колво баллов
        Assertions.assertTrue(correctFinalResponse.contains("10 из 10"),
                "При правильном ответе должны быть все баллы. Получено: " + correctFinalResponse);

        // для неправильного ответа ожидаем 0 баллов
        Assertions.assertTrue(incorrectFinalResponse.contains("0 из 10"),
                "При неправильном ответе баллы не должны начисляться. Получено: " + incorrectFinalResponse);
    }

    /**
     * Проверка на вывод результатов после теста на уровень A1-A2
     */
    @Test
    void testFinalAnswerMinimalLevel() {
        String customTestText = """
                1 (6 points)
                Test question?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        testHandler.generateTest(12345L, customTestText);
        String finalResponse = testHandler.handleAnswer("A_button", 12345L);

        String expectedResponse = "🎉 *Тест завершён!* 🎉\n\n" +

                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 0 из 6 возможных\n" +
                "📈 Уровень владения языком: A1-A2 (Начальный)\n\n" +

                "✨ **Отличная работа!** ✨\n\n";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }

    /**
     * Проверка на вывод результатов после теста на уровень C1-C2.
     */
    @Test
    void testFinalAnswerMaximalLevel() {
        String customTestText = """
                1 (14 points)
                Test question?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        testHandler.generateTest(12345L, customTestText);
        String finalResponse = testHandler.handleAnswer("B_button", 12345L);

        String expectedResponse = "🎉 *Тест завершён!* 🎉\n\n" +

                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 14 из 14 возможных\n" +
                "📈 Уровень владения языком: C1-C2 (Продвинутый)\n\n" +

                "✨ **Отличная работа!** ✨\n\n";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }

    /**
     * Проверка подсчета баллов при правильных и неправильных ответах, и в конце вывод на уровень B1-B2
     */
    @Test
    void testScoreCalculation() {
        String testText = """
                1 (6 points)
                Question 1?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                2 (6 points)
                Question 2?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                3 (6 points)
                Question 3?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        testHandler.generateTest(99999L, testText); // ДРУГОЙ chatId чтобы не конфликтовать
        testHandler.handleAnswer("B_button", 99999L);//вкрный ответ (+6 баллов)
        testHandler.handleAnswer("A_button", 99999L); //неверный ответ
        String finalResponse = testHandler.handleAnswer("B_button", 99999L);//верный ответ

        String expectedResponse = "🎉 *Тест завершён!* 🎉\n\n" +

                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 12 из 18 возможных\n" +
                "📈 Уровень владения языком: B1-B2 (Средний)\n\n" +

                "✨ **Отличная работа!** ✨\n\n";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }

    //     ТЕСТ НА SPEED_TEST
    /**
     * Тест обработки правильного ответа в speed тесте
     */
    @Test
    void testSpeedTestCorrectAnswer() {
        SpeedTestHandler speedTestHandler = new SpeedTestHandler();
        speedTestHandler.generateTest(99999L, testText);

        Map<String, Object> result = speedTestHandler.handleAnswerWithFeedback("B_button", 99999L);

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

        Map<String, Object> result = speedTestHandler.handleAnswerWithFeedback("A_button", 99998L);

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
        speedTestHandler.handleAnswerWithFeedback("B_button", 10002L);
        String question2 = speedTestHandler.moveToNextQuestion(10002L);
        int points2 = speedTestHandler.getCurrentQuestionPoints(10002L);
        Assertions.assertEquals(2, points2);

        // отвечаем и переходим к третьему вопросу (3 балла = 20 секунд)
        speedTestHandler.handleAnswerWithFeedback("B_button", 10002L);
        String question3 = speedTestHandler.moveToNextQuestion(10002L);
        int points3 = speedTestHandler.getCurrentQuestionPoints(10002L);
        Assertions.assertEquals(3, points3);
    }



    /**
     * Тест на очень низкий результат (меньше 30%)
     */
    @Test
    void testVeryLowResult() {

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
        speedTestHandler.handleAnswerWithFeedback("B_button", 20009L); // +3
        speedTestHandler.moveToNextQuestion(20009L);
        speedTestHandler.handleAnswerWithFeedback("A_button", 20009L); // +0
        speedTestHandler.moveToNextQuestion(20009L);
        speedTestHandler.handleAnswerWithFeedback("A_button", 20009L); // +0
        speedTestHandler.moveToNextQuestion(20009L);
        speedTestHandler.handleAnswerWithFeedback("A_button", 20009L); // +0

        String finalMessage = speedTestHandler.moveToNextQuestion(20009L);
        //фраза менятеся от баллов, а не от процента
        Assertions.assertEquals("💪 *Есть над чем поработать!* 💪\n\n" +
                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 3 из 12 возможных\n" +
                "📈 Процент выполнения: 25,0%\n\n" +
                "✨ **Продолжайте в том же духе!** ✨\n\n" +
                "Для продолжения работы используйте команды:\n" +
                "• /start - пройти тест заново\n" +
                "• /speed_test - тест на скорость\n" +
                "• /help - все доступные команды\n\n", finalMessage);
    }
    /**
     * Тест итогового сообщения со средним результатом (граница между категориями)
     */
    @Test
    void testFinalMessageAverageResult() {

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

        // граница между "Есть над чем поработать" и "Хороший результат"
        speedTestHandler.handleAnswerWithFeedback("B_button", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20004L); // +2
        speedTestHandler.moveToNextQuestion(20004L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20004L); // +2

        String finalMessage = speedTestHandler.moveToNextQuestion(20004L);
        //фраза менятеся от баллов, а не от процента
        Assertions.assertEquals("👍 *Хороший результат!* 👍\n\n" +
                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 12 из 12 возможных\n" +
                "📈 Процент выполнения: 100,0%\n\n" +
                "✨ **Продолжайте в том же духе!** ✨\n\n" +
                "Для продолжения работы используйте команды:\n" +
                "• /start - пройти тест заново\n" +
                "• /speed_test - тест на скорость\n" +
                "• /help - все доступные команды\n\n", finalMessage);
    }

    /**
     * Тест итогового сообщения с результатом на границе хорошего и отличного
     */
    @Test
    void testFinalMessageGoodToExcellentBoundary() {

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

        // 18 баллов из 24 - граница между "Хороший результат" и "Отличный результат"
        speedTestHandler.handleAnswerWithFeedback("B_button", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20005L); // +3
        speedTestHandler.moveToNextQuestion(20005L);
        speedTestHandler.handleAnswerWithFeedback("B_button", 20005L); // +3

        String finalMessage = speedTestHandler.moveToNextQuestion(20005L);
        //фраза менятеся от баллов, а не от процента
        Assertions.assertEquals("🎉 *Отличный результат!* 🎉\n\n" +
                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 18 из 18 возможных\n" +
                "📈 Процент выполнения: 100,0%\n\n" +
                "✨ **Продолжайте в том же духе!** ✨\n\n" +
                "Для продолжения работы используйте команды:\n" +
                "• /start - пройти тест заново\n" +
                "• /speed_test - тест на скорость\n" +
                "• /help - все доступные команды\n\n", finalMessage);
    }


    //              ТЕСТЫ НА КНОПКИ

    /**
     * Тест на наличие всех кнопок в стартовом словаре кнопок
     */
    @Test
    void testStartKeyboardButtons() {
        Map<String, String> startButtons = keyboardService.getStartButtonConfigs();

        // проверка для yes_button
        Assertions.assertEquals("yes_button", startButtons.get("Конечно!"));

        //проверка для no_button
        Assertions.assertEquals("no_button", startButtons.get("Назад"));

        // проверка на общее количество
        Assertions.assertEquals(2, startButtons.size());
    }
    /**
     * Тест на наличие всех кнопок в тестовом словаре кнопок
     */
    @Test
    void testAnswerKeyboardButtons() {
        Map<String, String> answerButtons = keyboardService.getTestAnswerConfigs();

        // проверяем все варианты ответов A, B, C, D
        Assertions.assertEquals("A_button", answerButtons.get("A"));
        Assertions.assertEquals("B_button", answerButtons.get("B"));
        Assertions.assertEquals("C_button", answerButtons.get("C"));
        Assertions.assertEquals("D_button", answerButtons.get("D"));

        // проверяем общее количество кнопок
        Assertions.assertEquals(4, answerButtons.size(),
                "Должно быть 4 кнопки в тестовой клавиатуре");
    }

    /**
     * Тест на наличие всех кнопок в стартовом словаре теста на сколрость
     */
    @Test
    void testSpeedKeyboardButtons() {
        Map<String, String> startButtons = keyboardService.getSpeedTestStartButton();
        Assertions.assertEquals("speed_yes_button", startButtons.get("Да"));
        Assertions.assertEquals("speed_no_button", startButtons.get("Нет"));
        Assertions.assertEquals(2, startButtons.size());
    }

    /**
     * Тест на наличие кнопки дальше в словаре
     */
    @Test
    void testSpeedNextKeyboardButtons() {
        Map<String, String> startButtons = keyboardService.getSpeedTestNextButton();
        Assertions.assertEquals("next_button", startButtons.get("Дальше"));
        Assertions.assertEquals(1, startButtons.size());
    }

    /**
     * Тест на обработку кнопки "Назад" (no_button)
     */
    @Test
    void testStartNoButtonProcessing() {
        String response = botLogic.processCallbackData("no_button", 12345L);

        String expectedResponse =
                "💪 *Не сомневайтесь в своих силах!* 💪\n\n" +
                        "📖 Тест займет всего несколько минут и поможет определить ваш текущий уровень\n\n" +
                        "🕐 Когда будете готовы - просто нажмите /start\n\n" +
                        "🔍 Все команды доступны по /help";

        Assertions.assertEquals(expectedResponse, response);

        // проверяем, что для no_button возвращается правильный тип кнопок
        String keyboardType = botLogic.getKeyboardForCallback("no_button", 12345L);
        Assertions.assertEquals(null, keyboardType);
    }


}