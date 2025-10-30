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

    /**
     * Тест на обработку кнопки "Назад" (no_button)
     */
    @Test
    void testNoButtonProcessing() {
        String response = botLogic.processCallbackData("no_button", 12345L);

        String expectedResponse =
                "💪 *Не сомневайтесь в своих силах!* 💪\n\n" +
                        "📖 Тест займет всего несколько минут и поможет определить ваш текущий уровень\n\n" +
                        "🕐 Когда будете готовы - просто нажмите /start\n\n" +
                        "🔍 Все команды доступны по /help";

        Assertions.assertEquals(expectedResponse, response);
    }

    /**
     * Тест на обработку кнопки "На Главную" (main_button) (по факту она выбает справку)
     */
    @Test
    void testMainButton() {
        String response = botLogic.processCallbackData("main_button", 12345L);

        String expectedResponse =
                "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

                        "📋 **Доступные команды:**\n\n" +
                        "• /start - Начать работу с ботом и пройти тестирование\n" +
                        "• /help - Показать эту справку\n" +

                        "🎯 **Как работает бот:**\n\n" +
                        "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
                        "• 📝 Тестирование для определения вашего уровня\n\n" +

                        "🛠️ **В процессе разработки:****\n" +
                        "• 🎮 Интерактивные упражнения\n" +
                        "• 📊 Отслеживание прогресса\n\n" +

                        "💡 **Как взаимодействовать:**\n" +
                        "• Используйте команды из меню (слева)\n" +
                        "• Нажимайте на кнопки под сообщениями\n" +
                        "• Отвечайте на вопросы теста\n" +
                        "• Следите за своим прогрессом в профиле\n\n" +

                        "🚀 **Начните с команды /start чтобы определить ваш уровень!**";

        Assertions.assertEquals(expectedResponse, response);
    }

    /**
     * Тест на обработку неизвестной кнопки
     */
    @Test
    void testUnknownButtonProcessing() {
        String response = botLogic.processCallbackData("unknown_button", 12345L);
        Assertions.assertEquals("Неизвестная команда", response);
    }

    /**
     * Тест на определение типа клавиатуры для разных callback данных
     * мы не рассматриваем кнопки A/B/C/D тк они зависимы от интернета и по этому если делать
     * проверку, то оно при выключенном интернете даст null
     */
    @Test
    void testGetKeyboardForCallback() {
        BotLogic botLogic = new BotLogic();

        // Для yes_button должна возвращаться test_answers
        Assertions.assertEquals("test_answers", botLogic.getKeyboardForCallback("yes_button", 12345L));

        // Для no_button должна возвращаться main
        Assertions.assertEquals("main", botLogic.getKeyboardForCallback("no_button", 12345L));

        // Для неизвестной кнопки - null
        Assertions.assertNull(botLogic.getKeyboardForCallback("unknown_button", 12345L));
    }

    /**
     * Тест на обработку текстовых сообщений
     */
    @Test
    void testHandleTextMessage() {
        BotLogic botLogic = new BotLogic();

        /**Вот так это выгляит формально
         * result = {
         * "chatId": "12345",
         * "text": "какойто текст",
         * "keyboardType": "start"
         }
         */

        // Обработка команды /start
        Map<String, String> result = botLogic.handleTextMessage(12345L, "/start");
        Assertions.assertEquals("12345", result.get("chatId"));
        Assertions.assertNotNull(result.get("text")); //Проверка на то что "text" не хранит null
        Assertions.assertEquals("start", result.get("keyboardType"));

        // Обработка обычного текста (не команды) - должен вернуть пустой результат
        Map<String, String> emptyResult = botLogic.handleTextMessage(12345L, "обычный текст");
        Assertions.assertTrue(emptyResult.isEmpty());

        // Обработка команды /help
        Map<String, String> helpResult = botLogic.handleTextMessage(12345L, "/help");
        Assertions.assertEquals("12345", helpResult.get("chatId"));
        Assertions.assertNotNull(helpResult.get("text"));
    }

    /**
     * Тест на распределение (маршрутизаию) входящих данных
     * Проверяет текстовое сообщение или кнопку от пользователя
     */
    @Test
    void testProcessInput() {
        BotLogic botLogic = new BotLogic();

        // Обработка callback
        Map<String, String> callbackResult = botLogic.processInput("callback", 12345L, "no_button");
        Assertions.assertEquals("12345", callbackResult.get("chatId")); //Проверка id, что совпадают

        // Обработка message
        Map<String, String> messageResult = botLogic.processInput("message", 12345L, "/start");
        Assertions.assertEquals("12345", messageResult.get("chatId"));

        // Неизвестный тип ввода
        Map<String, String> unknownResult = botLogic.processInput("unknown", 12345L, "data");
        Assertions.assertTrue(unknownResult.isEmpty()); //проверка на пустой результат
    }
}
