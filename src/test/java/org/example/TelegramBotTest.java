package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * TelegramBotTest - тесты на логику
 */


public class TelegramBotTest {
    private final LogicBot logicBot = new LogicBot();
    private final TestHandler testHandler = new TestHandler();

    /**
     * Проверка команды /start
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
     */
    @Test
    void testUnknownCommand() {
        String result = logicBot.handleCommand("/unknown", 12345L);

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
     * Проверка обработки кнопки.
     */
    @Test
    void testHandleCorrectAnswer() {
        String response = testHandler.handleAnswer("B_button", 12345L);

        String expectedQuestion = "Which word is a verb?\n" +
                "A. Apple\n" +
                "B. Run\n" +
                "C. Table\n" +
                "D. House";
        Assertions.assertEquals(expectedQuestion, response);
    }

    /**
     * Проверка на неправильный ответ
     */
    @Test
    void testHandleIncorrectAnswer() {
        String response = testHandler.handleAnswer("A_button", 12345L);

        String expectedQuestion = "Which word is a verb?\n" +
                "A. Apple\n" +
                "B. Run\n" +
                "C. Table\n" +
                "D. House";
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

        testHandler.generateTest(12345L, customTestText);
        String finalResponse = testHandler.handleAnswer("B_button", 12345L);

        String expectedResponse = "Тест завершён!\n\n" +
                "Вы набрали 6 баллов из 6 возможных.\n" +
                "Ваш уровень владения языком A1-A2 (Начальный)\n\n" +
                "Отличная работа!";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }

    /**
     * Проверка подсчета баллов при неправильных ответах
     */
    @Test
    void testScoreCalculation() {
        String testText = """
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

        testHandler.generateTest(99999L, testText); // ДРУГОЙ chatId чтобы не конфликтовать
        testHandler.handleAnswer("A_button", 99999L);
        testHandler.handleAnswer("A_button", 99999L);
        String finalResponse = testHandler.handleAnswer("A_button", 99999L);

        String expectedResponse = "Тест завершён!\n\n" +
                "Вы набрали 0 баллов из 6 возможных.\n" +
                "Ваш уровень владения языком A1-A2 (Начальный)\n\n" +
                "Отличная работа!";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }
}