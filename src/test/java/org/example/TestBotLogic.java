package org.example;

import org.example.Data.BotResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * TestBotLogic - тестирует обработку текстовых команд бота.
 * Проверяет правильность ответов на команды /start, /help и неизвестные команды.
 */

public class TestBotLogic {

    private final BotLogic botLogic = new BotLogic();

    /**
     * Проверка команды /start (неавторизованный пользователь)
     */
    @Test
    void testStartCommandUnauthorized() {
        BotResponse result = botLogic.processMessage("/start", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
                "Ваш персональный помощник в изучении иностранных языков! 🎯\n\n" +
                "📝 **Для начала работы необходимо зарегистрироваться**\n" +
                "Это займет всего 30 секунд, но откроет все возможности платформы!\n\n" +
                "✨ **После регистрации вы получите:**\n" +
                "• Персональную программу обучения\n" +
                "• Доступ к урокам и упражнениям\n" +
                "• Доступ к созданию личного словаря\n"+
                "• Трекинг прогресса\n\n" +
                "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения " +
                "вашего текущего уровня владения языком.\n\n" +
                "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
                "🎯 **Готовы открыть мир языков?**\n" +
                "🚀 Начните с регистрации и тестирования!", result.getText());
        Assertions.assertEquals("sing_in_main", result.getKeyboardType());
    }

    /**
     * Проверка команды /start_test (неавторизованный пользователь)
     */
    @Test
    void testStartTestCommandUnauthorized() {
        BotResponse result = botLogic.processMessage("/start_test", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
        Assertions.assertEquals("sing_in_main", result.getKeyboardType());
    }

    /**
     * Проверка команды /speed_test (неавторизованный пользователь)
     */
    @Test
    void testSpeedTestCommandUnauthorized() {
        BotResponse result = botLogic.processMessage("/speed_test", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
        Assertions.assertEquals("sing_in_main", result.getKeyboardType());
    }

    /**
     * Проверка команды /dictionary (неавторизованный пользователь)
     */
    @Test
    void testDictionaryCommandUnauthorized() {
        BotResponse result = botLogic.processMessage("/dictionary", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
        Assertions.assertEquals("sing_in_main", result.getKeyboardType());
    }

    /**
     * Проверка команды /my_profile (неавторизованный пользователь)
     */
    @Test
    void testMyProfileCommandUnauthorized() {
        BotResponse result = botLogic.processMessage("/my_profile", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
        Assertions.assertEquals("sing_in_main", result.getKeyboardType());
    }

    /**
     * Проверка команды /help (доступна без авторизации)
     */
    @Test
    void testHelpCommand() {
        BotResponse result = botLogic.processMessage("/help", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

                "📋 **Доступные команды:**\n" +
                "• /start- Начать работу с ботом\n" +
                "• /start_test - Пройти тест на уровень языка\n" +
                "• /help - Показать эту справку\n" +
                "• /dictionary - Работа со словарем\n" +
                "• /speed_test - Пройти тест на скорость\n\n" +

                "🎯 **Как работает бот:**\n" +
                "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
                "• 📝 Тестирование для определения вашего уровня\n" +
                "• 🎮 Интерактивные упражнения\n\n" +

                "🛠️ **В процессе разработки:**\n" +
                "• 📊 Отслеживание прогресса\n" +
                "• 📚 Словарь и словарный запас\n\n"+

                "💡 **Как взаимодействовать:**\n" +
                "• Используйте команды из меню (слева)\n" +
                "• Нажимайте на кнопки под сообщениями\n" +
                "• Отвечайте на вопросы теста\n" +
                "• Следите за своим прогрессом в профиле\n\n" +

                "🚀 **Начните с команды /start_test чтобы определить ваш уровень!**", result.getText());
        Assertions.assertNull(result.getKeyboardType());
    }

    /**
     * Проверка неизвестной команды
     */
    @Test
    void testUnknownCommand() {
        BotResponse result = botLogic.processMessage("/unknown", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("Неизвестная команда. Введите /help для списка доступных команд.", result.getText());
    }

    /**
     * Тест на обработку обычного текстового сообщения (не команды)
     */
    @Test
    void testRegularTextMessage() {
        BotResponse result = botLogic.processMessage("обычный текст", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("Не понимаю команду. Введите /help для справки.", result.getText());
    }

    /**
     * Тест на обработку кнопки "Назад" (no_button)
     */
    @Test
    void testNoButtonProcessing() {
        BotResponse result = botLogic.processCallback("no_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
        Assertions.assertEquals("main", result.getKeyboardType());
    }

    /**
     * Тест на обработку неизвестной кнопки
     */
    @Test
    void testUnknownButtonProcessing() {
        BotResponse result = botLogic.processCallback("unknown_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
    }

    /**
     * Тест на обработку кнопок ответов A/B/C/D (неавторизованный пользователь)
     */
    @Test
    void testAnswerButtonsProcessingUnauthorized() {
        BotResponse resultA = botLogic.processCallback("A_button", 12345L);
        BotResponse resultB = botLogic.processCallback("B_button", 12345L);

        Assertions.assertEquals(12345L, resultA.getChatId());
        Assertions.assertEquals(12345L, resultB.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", resultA.getText());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", resultB.getText());
    }

    /**
     * Тест на обработку кнопок аутентификации (доступны без авторизации)
     */
    @Test
    void testAuthButtonsProcessing() {
        BotResponse result = botLogic.processCallback("start_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("sing_in_main", result.getKeyboardType());
    }

    /**
     * Тест на обработку кнопок профиля (неавторизованный пользователь)
     */
    @Test
    void testProfileButtonsUnauthorized() {
        BotResponse result = botLogic.processCallback("my_profile_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
    }

    /**
     * Тест на обработку кнопок словаря (неавторизованный пользователь)
     */
    @Test
    void testDictionaryButtonsUnauthorized() {
        BotResponse result = botLogic.processCallback("dictionary_add_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.", result.getText());
    }

    /**
     * Тест на определение типа клавиатуры для разных callback данных
     */
    @Test
    void testGetKeyboardForCallback() {
        Assertions.assertEquals("test_answers", botLogic.getKeyboardForCallback("yes_button", 12345L));
        Assertions.assertEquals("main", botLogic.getKeyboardForCallback("no_button", 12345L));
        Assertions.assertNull(botLogic.getKeyboardForCallback("unknown_button", 12345L));
        Assertions.assertEquals("dictionary", botLogic.getKeyboardForCallback("dictionary_button", 12345L));
        Assertions.assertEquals("sing_in_main", botLogic.getKeyboardForCallback("start_button", 12345L));
        Assertions.assertEquals("my_profile", botLogic.getKeyboardForCallback("my_profile_button", 12345L));
    }

    /**
     * Тест определения клавиатуры для команды
     */
    @Test
    void testGetKeyboardForCommand() {
        Assertions.assertEquals("sing_in_main", botLogic.getKeyboardForCommand("/start"));
        Assertions.assertEquals("start", botLogic.getKeyboardForCommand("/start_test"));
        Assertions.assertEquals("speed_test_start", botLogic.getKeyboardForCommand("/speed_test"));
        Assertions.assertEquals("dictionary", botLogic.getKeyboardForCommand("/dictionary"));
        Assertions.assertEquals("my_profile", botLogic.getKeyboardForCommand("/my_profile"));
        Assertions.assertNull(botLogic.getKeyboardForCommand("/help"));
        Assertions.assertNull(botLogic.getKeyboardForCommand("/unknown"));
        Assertions.assertNull(botLogic.getKeyboardForCommand(null));
    }

    /**
     * Тест валидности BotResponse
     */
    @Test
    void testBotResponseValidity() {
        BotResponse validResponse = new BotResponse(12345L, "Valid text");
        BotResponse invalidResponse = new BotResponse(12345L, "");
        BotResponse nullResponse = new BotResponse(12345L, null);

        Assertions.assertTrue(validResponse.isValid());
        Assertions.assertFalse(invalidResponse.isValid());
        Assertions.assertFalse(nullResponse.isValid());
    }

    /**
     * Тест наличия клавиатуры в BotResponse
     */
    @Test
    void testBotResponseKeyboard() {
        BotResponse withKeyboard = new BotResponse(12345L, "Text", "start");
        BotResponse withoutKeyboard = new BotResponse(12345L, "Text");
        BotResponse emptyKeyboard = new BotResponse(12345L, "Text", "");

        Assertions.assertTrue(withKeyboard.hasKeyboard());
        Assertions.assertFalse(withoutKeyboard.hasKeyboard());
        Assertions.assertFalse(emptyKeyboard.hasKeyboard());
    }

    /**
     * Тест обработки пустого сообщения
     */
    @Test
    void testEmptyMessage() {
        BotResponse result = botLogic.processMessage("", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("Не понимаю команду. Введите /help для справки.", result.getText());
    }


    //ПОДУМАТЬ КАК НАПИСАТЬ ТЕСТЫ ДЛЯ АВТОРИЗОВАННОГО ПОЛЬЗОВАТЕЛЯ
}