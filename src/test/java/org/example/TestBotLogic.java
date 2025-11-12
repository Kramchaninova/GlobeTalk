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
     * Тест на обработку обычного текстового сообщения (не команды)
     */
    @Test
    void testRegularTextMessage() {
        BotResponse result = botLogic.processMessage("обычный текст", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌Неправильный ввод или команда", result.getText());
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
     * Тест на определение типа клавиатуры для разных callback данных
     */
    @Test
    void testGetKeyboardForCallback() {
        // Для yes_button должна возвращаться test_answers
        Assertions.assertEquals("test_answers", botLogic.getKeyboardForCallback("yes_button", 12345L));

        // Для no_button должна возвращаться main
        Assertions.assertEquals("main", botLogic.getKeyboardForCallback("no_button", 12345L));

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
     * Тест команды /speed_test
     */
    @Test
    void testSpeedTestCommand() {
        BotResponse result = botLogic.processMessage("/speed_test", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals( "🌍 *Добро пожаловать в тест на скорость!* 🌍\n\n" +
                "⚡ **Тест на скорость реакции** ⚡\n\n" +
                "Проверьте, насколько быстро вы можете отвечать на вопросы!\n\n" +
                "📊 **Как это работает:**\n" +
                "• Вам будут показаны вопросы с вариантами ответов\n" +
                "• Отвечайте как можно быстрее\n" +
                "• В конце получите статистику ответов\n\n" +
                "🎯 **Особенности теста:**\n" +
                "• Таймер отслеживает скорость ваших ответов\n" +
                "• Можно перейти к следующему вопросу кнопкой \"Дальше\"\n" +
                "• Результаты помогут оценить вашу реакцию\n\n" +
                "🚀 **Начнем тест на скорость?**", result.getText());
        Assertions.assertEquals("speed_test_start", result.getKeyboardType());
    }

    /**
     * Тест команды /dictionary
     */
    @Test
    void testDictionaryCommand() {
        BotResponse result = botLogic.processMessage("/dictionary", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь пуст*\n" +
                "Добавьте первое слово для начала изучения!\n\n"+
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить перевод слова\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:", result.getText());
        Assertions.assertEquals("dictionary", result.getKeyboardType());
    }

    /**
     * Тест main_button callback
     */
    @Test
    void testMainButtonCallback() {
        BotResponse result = botLogic.processCallback("main_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals(botLogic.COMMAND_HELP, result.getText());
        Assertions.assertNull(result.getKeyboardType());
    }

    /**
     * Тест словарных кнопок
     */
    @Test
    void testDictionaryButtons() {
        BotResponse addButton = botLogic.processCallback("dictionary_add_button", 12345L);
        BotResponse editButton = botLogic.processCallback("dictionary_edit_button", 12345L);
        BotResponse deleteButton = botLogic.processCallback("dictionary_delete_button", 12345L);

        Assertions.assertEquals("📝 *Как добавить слово:*\n\n" +
                "Просто отправьте мне слово на иностранном языке, а затем его перевод через пробел.\n" +
                "А если хотите добавить фразу и перевод, то введите их через тире ('-') \n\n" +
                "*Например:*\n" +
                "`apple - яблоко`\n" +
                "`looking for - искать (находиться в поиске)`", addButton.getText());

        Assertions.assertEquals("🔤 Редактирование перевода\n" +
                "Чтобы отредактировать слово, введите его на английском языке " +
                "в точности так, как оно указано в словаре. Изменить можно только " +
                "его перевод на русский язык.", editButton.getText());

        Assertions.assertEquals("🗑️ *Как удалить слово:*\n\n" +
                "Просто отправьте мне слово на английском (без перевода), которое хотите удалить из словаря.\n\n" +
                "*Например:*\n" +
                "вы хотите удалить \"apple - яблоко\"\n" +
                "введите: \"apple\"\n\n" +
                "✨ *После удаления слово перестанет появляться в ваших тренировках!*", deleteButton.getText());
    }

    /**
     * Тест определения клавиатуры для словарных callback
     */
    @Test
    void testGetKeyboardForDictionaryCallbacks() {
        Assertions.assertEquals("dictionary", botLogic.getKeyboardForCallback("dictionary_button", 12345L));
        Assertions.assertEquals("dictionary", botLogic.getKeyboardForCallback("dictionary_add_no_button", 12345L));
        Assertions.assertEquals("delete_cancel", botLogic.getKeyboardForCallback("dictionary_delete_cancel_button", 12345L));
        Assertions.assertEquals("dictionary_final_button", botLogic.getKeyboardForCallback("dictionary_delete_confirm_button", 12345L));
    }


    /**
     * Тест next_button callback
     */
    @Test
    void testNextButtonCallback() {
        BotResponse result = botLogic.processCallback("next_button", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("Тест не активен", result.getText());
    }

    /**
     * Тест обработки пустого сообщения в словаре
     */
    @Test
    void testEmptyMessage() {
        BotResponse result = botLogic.processMessage("", 12345L);

        Assertions.assertEquals(12345L, result.getChatId());
        Assertions.assertEquals("❌Неправильный ввод или команда", result.getText());
    }
}