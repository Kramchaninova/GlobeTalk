package org.example;

import java.util.Map;

/**
 * BotLogic - класс для обработки логики бота.
 * Обрабатывает команды, кнопки и ответы пользователей.
 */
public class BotLogic {
    private final StartCommand startCommand;
    private final TestHandler testHandler;
    private final KeyboardService keyboardService;

    public BotLogic() {
        this.testHandler = new TestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.keyboardService = new KeyboardService();
    }

    private static final String COMMAND_HELP = "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +
            "📋 **Доступные команды:**\n\n" +
            "• /start - Начать работу с ботом и пройти тестирование\n" +
            "• /help - Показать эту справку\n" +
            "🎯 **Как работает бот:**\n\n" +
            "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
            "• 📝 Тестирование для определения вашего уровня\n\n" +
            "🛠️ **В процессе разработки:**\n" +
            "• 🎮 Интерактивные упражнения\n" +
            "• 📊 Отслеживание прогресса\n\n" +
            "💡 **Как взаимодействовать:**\n" +
            "• Используйте команды из меню (слева)\n" +
            "• Нажимайте на кнопки под сообщениями\n" +
            "• Отвечайте на вопросы теста\n" +
            "• Следите за своим прогрессом в профиле\n\n" +
            "🚀 **Начните с команды /start чтобы определить ваш уровень!**";

    private static final String COMMAND_UNKNOWN = "Неизвестная команда. Введите /help для списка доступных команд.";

    /** Обработка нажатий на кнопки */
    public String[] handleCallbackData(String callbackData, long chatId) {
        String responseText;
        String keyboardType = null;

        if ("A_button".equals(callbackData) || "B_button".equals(callbackData) ||
                "C_button".equals(callbackData) || "D_button".equals(callbackData)) {
            responseText = testHandler.handleAnswer(callbackData, chatId);
            if (testHandler.isTestActive(chatId)) keyboardType = "test_answers";
        } else {
            responseText = startCommand.handleButtonClick(callbackData, chatId);
            if ("yes_button".equals(callbackData)) keyboardType = "test_answers";
            if ("no_button".equals(callbackData)) keyboardType = "start";
        }

        return new String[]{responseText, keyboardType};
    }

    /** Обработка команд /start, /help и других */
    public String[] handleCommand(String command, long chatId) {
        String responseText;
        String keyboardType = null;

        switch (command) {
            case "/start":
                responseText = startCommand.startTest();
                keyboardType = "start";
                break;
            case "/help":
                responseText = COMMAND_HELP;
                break;
            default:
                responseText = COMMAND_UNKNOWN;
        }

        return new String[]{responseText, keyboardType};
    }

    /** Универсальный метод обработки входящих данных */
    public String[] processInput(String inputType, long chatId, String data) {
        if ("callback".equals(inputType)) {
            return handleCallbackData(data, chatId);
        } else if ("message".equals(inputType)) {
            if (data.startsWith("/")) {
                return handleCommand(data, chatId);
            }
        }
        return null;
    }

    /** Методы для передачи словарей кнопок */
    public Map<String, String> getStartButtons() {
        return keyboardService.getStartButtons();
    }

    public Map<String, String> getTestButtons() {
        return keyboardService.getTestButtons();
    }
}
