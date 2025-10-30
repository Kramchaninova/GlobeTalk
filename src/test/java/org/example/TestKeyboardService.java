package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
    void testMainKeyboardButton(){
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
    void testMainButton(){
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
}
