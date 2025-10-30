package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * BotLogic - класс для обработки логики бота.
 * обрабатывает входящие сообщения, команды и callback запросы от кнопок
 */

public class BotLogic {
    private final StartCommand startCommand;
    private final TestHandler testHandler;
    private final KeyboardService keyboardService;

    public BotLogic(){
        this.testHandler = new TestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.keyboardService = new KeyboardService();
    }

    private static final String COMMAND_HELP =  "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

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


    private static final String COMMAND_UNKNOWN = "Неизвестная команда. Введите /help для списка доступных команд.";

    /**
     * Обработка ответов с кнопок
     */
    public String processCallbackData(String callbackData, long chatId) {
        if (callbackData.equals("A_button") ||
                callbackData.equals("B_button") ||
                callbackData.equals("C_button") ||
                callbackData.equals("D_button")) {
            return testHandler.handleAnswer(callbackData, chatId);
        } else {
            return startCommand.handleButtonClick(callbackData, chatId);
        }
    }

    /**
     * Если в сообщении была команда, т.е. текст начинается с /, то обрабатываем ее
     * и высылаем текст, который привязан к командам
     */
    public String handleCommand(String command) {
        switch (command) {
            case "/start":
                // StartCommand - отельный класс для реализации старта бота,в дальнейшем логично было бы на каждую задачу выводить по классу
                return startCommand.startTest();

            case "/help":
                return COMMAND_HELP;

            default:
                return COMMAND_UNKNOWN;
        }

    }

    /**
     * handleCallbackQuery - собирает результаты обработки в Map
     */
    public Map<String, String> handleCallbackQuery(String callbackData, long chatId) {
        String responseText = processCallbackData(callbackData, chatId);
        String keyboardType = getKeyboardForCallback(callbackData, chatId);

        Map<String, String> result = new HashMap<>();
        result.put("chatId", String.valueOf(chatId));
        result.put("text", responseText);
        result.put("keyboardType", keyboardType != null ? keyboardType : "");

        return result;
    }

    // обработка всех входящих сообщений
    public Map<String, String> handleTextMessage(long chatId, String messageText) {
        Map<String, String> result = new HashMap<>();
        // команда из бокового меню
        if (messageText.startsWith("/")) {
            String responseText = handleCommand(messageText);
            String keyboardType = getKeyboardForCommand(messageText);

            result.put("chatId", String.valueOf(chatId));
            result.put("text", responseText);
            result.put("keyboardType", keyboardType != null ? keyboardType : "");

            System.out.println("Обработана команда из бокового меню: " + messageText);
        }

        return result;
    }

    /**
     * метод для распределения входящих данных на кнопки и текст
     */
    public Map<String, String> processInput(String inputType, long chatId, String data) {
        if ("callback".equals(inputType)) {
            return handleCallbackQuery(data, chatId);
        } else if ("message".equals(inputType)) {
            return handleTextMessage(chatId, data);
        }
        return new HashMap<>();
    }

    /**
     *  метод определения ключа показываемого списка кнопок после нажатия
     */
    public String getKeyboardForCallback(String callbackData, long chatId) {
        switch (callbackData) {
            case "yes_button" -> { return "test_answers"; }
            case "A_button", "B_button", "C_button", "D_button" -> {
                if (testHandler.isTestActive(chatId)) { return "test_answers"; }
            }
            case "no_button" -> {return  "main";}
            case "main_button" -> {}
        }
        return null;
    }

    /**
     * логика определения типа команды в боковом меню
     */
    public String getKeyboardForCommand(String command) {
        if (command != null && command.equals("/start")) {
            return "start";
        }
        return null;
    }

    public Map<String, String> getStartButtonConfigs() {
        return keyboardService.getStartButtonConfigs();
    }
    public Map<String, String> getTestAnswerConfigs() {
        return keyboardService.getTestAnswerConfigs();
    }
    public Map<String, String> getMainButtonCallBack() {return keyboardService.getMainButtonCallBack();}

}
