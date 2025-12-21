package org.example;

import org.example.Data.KeyboardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Map;

/**
 * TestKeyboardService - тестирует работу клавиатуры и обработку пользовательского ввода.
 * Проверяет: конфигурации кнопок (текст и callback), обработку нажатий кнопок,
 * маршрутизацию сообщений и callbackов, Формирование ответов бота
 */

public class TestKeyboardService {
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
        Assertions.assertEquals(4, answerButtons.size());
    }

    /**
     * Тест на наличие кнопки в словаре
     */
    @Test
    void testMainKeyboardButton() {
        Map<String, String> answerButtons = keyboardService.getMainButtonCallBack();
        Assertions.assertEquals("main_button", answerButtons.get("На главную"));
    }

    /**
     * Тест кнопок speed test start
     */
    @Test
    void testSpeedTestStartButtons() {
        Map<String, String> speedButtons = keyboardService.getSpeedTestStartButton();

        Assertions.assertEquals("speed_yes_button", speedButtons.get("Да"));
        Assertions.assertEquals("speed_no_button", speedButtons.get("Нет"));
        Assertions.assertEquals(2, speedButtons.size());
    }

    /**
     * Тест кнопки next для speed test
     */
    @Test
    void testSpeedTestNextButton() {
        Map<String, String> nextButton = keyboardService.getSpeedTestNextButton();

        Assertions.assertEquals("next_button", nextButton.get("Дальше"));
        Assertions.assertEquals(1, nextButton.size());
    }

    /**
     * Тест главных кнопок словаря
     */
    @Test
    void testDictionaryMainButtons() {
        Map<String, String> dictButtons = keyboardService.getDictionaryMainButton();

        Assertions.assertEquals("dictionary_add_button", dictButtons.get("Добавить"));
        Assertions.assertEquals("dictionary_edit_button", dictButtons.get("Редактировать"));
        Assertions.assertEquals("dictionary_delete_button", dictButtons.get("Удалить"));
        Assertions.assertEquals("main_button", dictButtons.get("Назад"));
        Assertions.assertEquals(4, dictButtons.size());
    }

    /**
     * Тест кнопок повторного добавления в словарь
     */
    @Test
    void testDictionaryAddAgainButtons() {
        Map<String, String> addAgainButtons = keyboardService.getDictionaryAddAgainButton();

        Assertions.assertEquals("dictionary_add_yes_button", addAgainButtons.get("Да"));
        Assertions.assertEquals("dictionary_add_no_button", addAgainButtons.get("Нет"));
        Assertions.assertEquals(2, addAgainButtons.size());
    }

    /**
     * Тест кнопок подтверждения удаления
     */
    @Test
    void testDictionaryDeleteButtons() {
        Map<String, String> deleteButtons = keyboardService.getDictionaryDeleteButton();

        Assertions.assertEquals("dictionary_delete_confirm_button", deleteButtons.get("Подтвердить"));
        Assertions.assertEquals("dictionary_delete_cancel_button", deleteButtons.get("Отменить"));
        Assertions.assertEquals(2, deleteButtons.size());
    }

    /**
     * Тест кнопок отмены удаления
     */
    @Test
    void testDictionaryDeleteCancelButtons() {
        Map<String, String> cancelButtons = keyboardService.getDictionaryDeleteCancelButton();

        Assertions.assertEquals("dictionary_delete_resume_button", cancelButtons.get("Продлолжить"));
        Assertions.assertEquals("main_button", cancelButtons.get("На главную"));
        Assertions.assertEquals("dictionary_button", cancelButtons.get("Словарь"));
        Assertions.assertEquals(3, cancelButtons.size());
    }

    /**
     * Тест финальных кнопок словаря
     */
    @Test
    void testDictionaryFinalButtons() {
        Map<String, String> finalButtons = keyboardService.getDictionaryFinalButton();

        Assertions.assertEquals("dictionary_button", finalButtons.get("Словарь"));
        Assertions.assertEquals("main_button", finalButtons.get("На главную"));
        Assertions.assertEquals(2, finalButtons.size());
    }
}