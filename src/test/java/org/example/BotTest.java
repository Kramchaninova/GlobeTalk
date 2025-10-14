package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BotTest {
    private final Bot likeBot = new Bot("test-token", "TestBot");
    private final LogicBot logicBot = new LogicBot(likeBot);
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
     *
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
     *
     */
    @Test
    void testHandleCorrectAnswer() {
        // Отправляем правильный ответ (вопрос 1 - правильный B)
        String response = testManager.handleAnswer("B_button", 12345L);

        // Проверяем, что возвращён следующий вопрос
        assertNotNull(response);
        assertTrue(response.contains("Which word is a verb?"), "Должен быть следующий вопрос");
    }

    /**
     * Проверка на неправильный ответ
     */
    @Test
    void testHandleIncorrectAnswer() {
        // Отправляем неправильный ответ (вопрос 1 - правильный B, мы нажимаем A)
        String response = testManager.handleAnswer("A_button", 12345L);

        // Проверяем, что мы всё равно перешли к следующему вопросу
        assertNotNull(response);
        assertTrue(response.contains("Which word is a verb?"));
    }

    /**
     * Проверка на вывод результатов после теста.
     */
    @Test
    void testFinalAnswer() {
        // Ответы: B (правильно), B (правильно), B (правильно)
        testManager.handleAnswer("B_button", 12345L);
        testManager.handleAnswer("B_button", 12345L);
        String finalResponse = testManager.handleAnswer("B_button", 12345L);

        // Проверяем, что тест завершён и подсчитаны баллы
        assertNotNull(finalResponse);
        assertTrue(finalResponse.contains("Тест завершён!"));
        assertTrue(finalResponse.contains("Вы набрали"));
    }
}
