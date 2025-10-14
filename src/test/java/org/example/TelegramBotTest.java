package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * TelegramBotTest - юнит тесты для проверки логики бота.
 * Здесь он проверяет комнады ("/"), правильную обработку сообщения и саму генерации от ии,
 * проверка обработки кнопок, итогового сообщения (с финальными резуальтатими теста),
 * проверка на праавильный подсчет баллов для определения уровня
 */

public class TelegramBotTest {

    private final LogicBot logicBot = new LogicBot();
    private final TestManager testManager = new TestManager();

    /**
     * Проверка команды /start
     * Проверяет приветственное сообщение с описанием бота и создателями.
     * Убеждается что есть ссылка на помощь и корректный ID чата.
     */

    @Test
    void testStartCommand() {
        String result = logicBot.handleCommand("/start", 12345L);

        Assertions.assertEquals("Вас приветствует телеграмм бот GlobeTalk для изучения иностранных языков!\n\n" +
                "Перед началом обучения, пройдите короткий тестик для определения вашего уровня владения языка.\n\n" +
                "Для списка команд нажмите /help.\n\n"+
                "Вы готовы начать тест?\n\n", result);
    }

    /**
     * Проверка команды /help
     * Проверяет наличие списка команд и описания работы бота.
     */

    @Test
    void testHelpCommand() {
        String result = logicBot.handleCommand("/help", 12345L);

        Assertions.assertEquals("  **Список доступных команд:**\n\n" +
                "'/start' - начать работу с ботом\n" +
                "'/help' - показать эту справку\n" +
                "     **Как взаимодействовать с ботом:**\n" +
                "Телеграмм бот работает по принципу ввода сообщение:\n" +
                "- если сообщение начинается не '/' то он просто повторяет\n" +
                "- если же начинается с '/' то он воспринимает это как команду", result);
    }

    /**
     * Проверка неизвестной команды
     * Проверяет сообщение об ошибке для неподдерживаемых команд.
     * а также, что бот предлагает помощь
     */

    @Test
    void testUnknownCommand() {
        String result = logicBot.handleCommand("/unknown", 12345L);

        Assertions.assertEquals("Неизвестная команда. Введите /help для списка доступных команд.", result);
    }

    String testText;

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

        testManager.generateTest(12345L, testText);
    }

    /**
     * Проверка обработки кнопки.
     */
    @Test
    void testHandleCorrectAnswer() {
        // Отправляем правильный ответ (вопрос 1 - правильный B)
        String response = testManager.handleAnswer("B_button", 12345L);

        String expectedQuestion = "Which word is a verb?\n\nA. Apple\nB. Run\nC. Table\nD. House";
        Assertions.assertEquals(expectedQuestion, response);
    }

    /**
     * Проверка на неправильный ответ
     */
    @Test
    void testHandleIncorrectAnswer() {
        // Отправляем неправильный ответ (вопрос 1 - правильный B, мы нажимаем A)
        String response = testManager.handleAnswer("A_button", 12345L);
        String expectedQuestion = "Which word is a verb?\n\nA. Apple\nB. Run\nC. Table\nD. House";
        Assertions.assertEquals(expectedQuestion, response);
    }

    /**
     * Проверка на вывод результатов после теста.
     */
    @Test
    void testFinalAnswer() {
        String customTestText = """
        1 (6 points)
        Test question?
        A. Wrong
        B. Correct
        C. Wrong
        D. Wrong
        Answer: B
        """;

        testManager.generateTest(12345L, customTestText);
        String finalResponse = testManager.handleAnswer("B_button", 12345L);

        // 6 баллов - это верхняя граница A2
        String expectedResponse = "Тест завершён!\n\n" +
                "Вы набрали 6 баллов из 6 возможных.\n" +
                "Ваш уровень владения языком A1-A2 (Начальный)\n\n" +
                "Отличная работа!";

        Assertions.assertEquals(expectedResponse, finalResponse,
                "При 6 баллах должен определяться уровень A1-A2");
    }


    /**
     * проверка на исправный подсчет баллов, при наихудшем варианте
     */
    @Test
    void testScoreCalculation() {
        // Ответы: A (неправильно), A (неправильно), A (неправильно)
        testManager.handleAnswer("A_button", 12345L);
        testManager.handleAnswer("A_button", 12345L);
        String finalResponse = testManager.handleAnswer("A_button", 12345L);

        String expectedResponse = "Тест завершён!\n\n" +
                "Вы набрали 0 баллов из 6 возможных.\n" +
                "Ваш уровень владения языком A1-A2 (Начальный)\n\n" +
                "Отличная работа!";

        Assertions.assertEquals(expectedResponse, finalResponse,
                "При всех неправильных ответах должен быть 0 баллов и начальный уровень");
    }
}
