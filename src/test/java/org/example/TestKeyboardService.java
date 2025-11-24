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

        Assertions.assertEquals("yes_button", startButtons.get("Конечно!"));
        Assertions.assertEquals("no_button", startButtons.get("Назад"));
        Assertions.assertEquals(2, startButtons.size());
    }

    /**
     * Тест на наличие всех кнопок в тестовом словаре кнопок
     */
    @Test
    void testAnswerKeyboardButtons() {
        Map<String, String> answerButtons = keyboardService.getTestAnswerConfigs();

        Assertions.assertEquals("A_button", answerButtons.get("A"));
        Assertions.assertEquals("B_button", answerButtons.get("B"));
        Assertions.assertEquals("C_button", answerButtons.get("C"));
        Assertions.assertEquals("D_button", answerButtons.get("D"));
        Assertions.assertEquals(4, answerButtons.size());
    }

    /**
     * Тест на наличие кнопки в словаре
     */
    @Test
    void testMainKeyboardButton() {
        Map<String, String> answerButtons = keyboardService.getMainButtonCallBack();
        Assertions.assertEquals("main_button", answerButtons.get("На главную"));
        Assertions.assertEquals(1, answerButtons.size());
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

    /**
     * Тест кнопок аутентификации - главное меню входа
     */
    @Test
    void testSingInMainButtons() {
        Map<String, String> authButtons = keyboardService.getSingInMain();

        Assertions.assertEquals("sing_in_button", authButtons.get("Войти"));
        Assertions.assertEquals("reg_button", authButtons.get("Зарегистрироваться"));
        Assertions.assertEquals(2, authButtons.size());
    }

    /**
     * Тест кнопок завершения аутентификации
     */
    @Test
    void testSingInEndButtons() {
        Map<String, String> endButtons = keyboardService.getSingInEnd();

        Assertions.assertEquals("sing_in_button", endButtons.get("Войти"));
        Assertions.assertEquals("start_button", endButtons.get("В начало"));
        Assertions.assertEquals(2, endButtons.size());
    }

    /**
     * Тест кнопок ошибки логина
     */
    @Test
    void testLoginErrorButtons() {
        Map<String, String> errorButtons = keyboardService.getLoginError();

        Assertions.assertEquals("login_again_button", errorButtons.get("Еще раз"));
        Assertions.assertEquals("start_button", errorButtons.get("В начало"));
        Assertions.assertEquals(2, errorButtons.size());
    }

    /**
     * Тест кнопок профиля пользователя
     */
    @Test
    void testMyProfileButtons() {
        Map<String, String> profileButtons = keyboardService.getMyProfile();

        Assertions.assertEquals("login_edit_button", profileButtons.get("Логин"));
        Assertions.assertEquals("password_edit_button", profileButtons.get("Пароль"));
        Assertions.assertEquals("log_out_button", profileButtons.get("Выйти"));
        Assertions.assertEquals(3, profileButtons.size());
    }

    /**
     * Тест кнопок завершения редактирования логина/пароля
     */
    @Test
    void testLoginPasswordEditEndButtons() {
        Map<String, String> editEndButtons = keyboardService.getLoginPasswordEditEnd();

        Assertions.assertEquals("my_profile_button", editEndButtons.get("Мой профиль"));
        Assertions.assertEquals("main_button", editEndButtons.get("На главную"));
        Assertions.assertEquals(2, editEndButtons.size());
    }

    /**
     * Тест кнопок подтверждения выхода
     */
    @Test
    void testLogOutConfirmationButtons() {
        Map<String, String> logoutButtons = keyboardService.getLogOutConfirmation();

        Assertions.assertEquals("log_out_final_button", logoutButtons.get("Выйти"));
        Assertions.assertEquals("log_out_cancel_button", logoutButtons.get("Остаться"));
        Assertions.assertEquals(2, logoutButtons.size());
    }

    /**
     * Тест кнопок отложенных сообщений
     */
    @Test
    void testScheduleMessageButtons() {
        Map<String, String> scheduleButtons = keyboardService.getScheduleMessage();

        Assertions.assertEquals("know_button", scheduleButtons.get("Знаю"));
        Assertions.assertEquals("learn_button", scheduleButtons.get("Изучаю"));
        Assertions.assertEquals(2, scheduleButtons.size());
    }

    /**
     * Тест финальных кнопок отложенных сообщений
     */
    @Test
    void testScheduleMessageFinalButtons() {
        Map<String, String> finalButtons = keyboardService.getScheduleMessageFinal();

        Assertions.assertEquals("dictionary_button", finalButtons.get("Словарь"));
        Assertions.assertEquals("more_word_button", finalButtons.get("Еще слова"));
        Assertions.assertEquals(2, finalButtons.size());
    }

    /**
     * Тест кнопок отложенных тестов
     */
    @Test
    void testScheduleTestYesOrNoButtons() {
        Map<String, String> testButtons = keyboardService.getScheduleTestYesOrNo();

        Assertions.assertEquals("yes_schedule_test_button", testButtons.get("Конечно!"));
        Assertions.assertEquals("no_schedule_test_button", testButtons.get("Нет:("));
        Assertions.assertEquals(2, testButtons.size());
    }

    /**
     * Тест на неизменяемость оригинальных коллекций
     */
    @Test
    void testCollectionsAreImmutable() {
        Map<String, String> original = keyboardService.getStartButtonConfigs();
        original.put("Новая кнопка", "new_button");

        Map<String, String> freshCopy = keyboardService.getStartButtonConfigs();
        Assertions.assertFalse(freshCopy.containsKey("Новая кнопка"));
        Assertions.assertEquals(2, freshCopy.size());
    }
}