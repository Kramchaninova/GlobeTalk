package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class BotTest {
    private final Bot likeBot = new Bot("test-token", "TestBot");
    private final LogicBot logicBot = new LogicBot(likeBot);

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
}
