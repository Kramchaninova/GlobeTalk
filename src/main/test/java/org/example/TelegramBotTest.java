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

                "📋 **Доступные команды:**\n\n" +
                "• /start - Начать работу с ботом и пройти тестирование\n" +
                "• /help - Показать эту справку\n" +

                "🎯 **Как работает бот:**\n\n" +
                "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
                "• 📝 Тестирование для определения вашего уровня\n\n" +

                "🛠️ **В процессе разработки:****\n" +
                "• 🎮 Интерактивные упражнения\n" +
                "• 📊 Отслеживание прогресса\n\n" +

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
     * Тест на обработку кнопки "Назад" (no_button)
     */
    @Test
    void testNoButtonProcessing() {
        String response = botLogic.processCallbackData("no_button", 12345L);

        String expectedResponse =
                "💪 *Не сомневайтесь в своих силах!* 💪\n\n" +
                        "📖 Тест займет всего несколько минут и поможет определить ваш текущий уровень\n\n" +
                        "🕐 Когда будете готовы - просто нажмите /start\n\n" +
                        "🔍 Все команды доступны по /help";

        Assertions.assertEquals(expectedResponse, response);

        // проверяем, что для no_button возвращается правильный тип кнопок
        String keyboardType = botLogic.getKeyboardForCallback("no_button", 12345L);
        Assertions.assertEquals("start", keyboardType);
    }

}