package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * TestBotLogic - тестирует обработку текстовых команд бота.
 * Проверяет правильность ответов на команды /start, /help и неизвестные команды.
 */

public class TestBotLogic {

    private final BotLogic botLogic = new BotLogic();

    /**
     * Проверка команды /start
     */
    @Test
    void testStartCommand() {
        BotResponse result = botLogic.processMessage("/start", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
                "Ваш персональный помощник в изучении иностранных языков! 🎯\n\n" +
                "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n" +
                "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
                "🔍 Для просмотра всех команд нажмите /help\n\n" +
                "🚀 **Вы готовы начать тест?**", result.getText());
        Assertions.assertEquals("start", result.getKeyboardType());
    }

    /**
     * Проверка команды /help
     */
    @Test
    void testHelpCommand() {
        BotResponse result = botLogic.processMessage("/help", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
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

                "🚀 **Начните с команды /start чтобы определить ваш уровень!**", result.getText());
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
        Assertions.assertEquals("main", keyboardType);
    }

    /**
     * Тест на обработку обычного текстового сообщения (не команды)
     */
    @Test
    void testRegularTextMessage() {
        BotResponse result = botLogic.processMessage("обычный текст", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("Сообщение получено", result.getText());
    }

    /**
     * Тест на обработку кнопки "Назад" (no_button)
     */
    @Test
    void testNoButtonProcessing() {
        BotResponse result = botLogic.processCallback("no_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("💪 *Не сомневайтесь в своих силах!* 💪\n\n" +
                "📖 Тест займет всего несколько минут и поможет определить ваш текущий уровень\n\n" +
                "🕐 Когда будете готовы - просто нажмите /start\n\n" +
                "🔍 Все команды доступны по /help", result.getText());
        Assertions.assertEquals("main", result.getKeyboardType());
    }

    /**
     * Тест на обработку неизвестной кнопки
     */
    @Test
    void testUnknownButtonProcessing() {
        BotResponse result = botLogic.processCallback("unknown_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("Неизвестная команда", result.getText());
    }

    /**
     * Тест на обработку кнопок ответов A/B/C/D (базовая проверка без зависимостей)
     */
    @Test
    void testAnswerButtonsProcessing() {
        // Проверяем, что методы не падают и возвращают корректную структуру
        BotResponse resultA = botLogic.processCallback("A_button", 12345L);
        BotResponse resultB = botLogic.processCallback("B_button", 12345L);

        Assertions.assertEquals(12345L, resultA.getChatId());
        Assertions.assertEquals(12345L, resultB.getChatId());
        Assertions.assertNotNull(resultA.getText());
        Assertions.assertNotNull(resultB.getText());
    }

    /**
     * Тест на обработку кнопки "На Главную" (main_button)
     */
    @Test
    void testMainButtonProcessing() {
        BotResponse result = botLogic.processCallback("main_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertNotNull(result.getText());
    }

    /**
     * Тест на обработку кнопки "Дальше" (next_button)
     */
    @Test
    void testNextButtonProcessing() {
        BotResponse result = botLogic.processCallback("next_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertNotNull(result.getText());
    }

    /**
     * Тест на определение типа клавиатуры для разных callback данных
     */
    @Test
    void testGetKeyboardForCallback() {
        // Для yes_button должна возвращаться test_answers
        Assertions.assertEquals("test_answers", botLogic.getKeyboardForCallback("yes_button", 12345L));

        // Для no_button должна возвращаться main
        Assertions.assertEquals("main", botLogic.getKeyboardForCallback("no_button", 12345L));

        // Для speed_yes_button должна возвращаться test_answers
        Assertions.assertEquals("test_answers", botLogic.getKeyboardForCallback("speed_yes_button", 12345L));

        // Для неизвестной кнопки - null
        Assertions.assertNull(botLogic.getKeyboardForCallback("unknown_button", 12345L));
    }

    /**
     * Тест определения клавиатуры для команды
     */
    @Test
    void testGetKeyboardForCommand() {
        // для команды /start
        Assertions.assertEquals("start", botLogic.getKeyboardForCommand("/start"));

        // для команды /speed_test
        Assertions.assertEquals("speed_test_start", botLogic.getKeyboardForCommand("/speed_test"));

        // для команды /help
        Assertions.assertNull(botLogic.getKeyboardForCommand("/help"));

        // для неизвестной команды
        Assertions.assertNull(botLogic.getKeyboardForCommand("/unknown"));

        // для null команды
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
     * Тест получения KeyboardService
     */
    @Test
    void testGetKeyboardService() {
        KeyboardService keyboardService = botLogic.getKeyboardService();
        Assertions.assertNotNull(keyboardService);

        // Проверяем, что возвращаемый объект работает корректно
        Map<String, String> startButtons = keyboardService.getStartButtonConfigs();
        Assertions.assertEquals(2, startButtons.size());
        Assertions.assertEquals("yes_button", startButtons.get("Конечно!"));
    }
}
