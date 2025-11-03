package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * TestKeyboardService - тестирует работу клавиатуры и обработку пользовательского ввода.
 * Проверяет: конфигурации кнопок (текст и callback), обработку нажатий кнопок,
 * маршрутизацию сообщений и callbackов, Формирование ответов бота
 */

public class TestKeyboardService {

    private final BotLogic botLogic = new BotLogic();
    private final KeyboardService keyboardService = new KeyboardService();

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
     * Тест на наличие одной кнопки в словаре
     */
    @Test
    void testMainKeyboardButton() {
        Map<String, String> answerButtons = keyboardService.getMainButtonCallBack();
        Assertions.assertEquals("main_button", answerButtons.get("На Главную"));
    }
}
