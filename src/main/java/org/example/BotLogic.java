package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * BotLogic - класс для обработки логики бота.
 * обрабатывает входящие сообщения, команды и callback запросы от кнопок
 */


public class BotLogic {
    private final StartCommand startCommand;
    private final SpeedTestCommand speedTestCommand;
    private final TestHandler testHandler;
    private final KeyboardService keyboardService;
    private final SpeedTestHandler speedTestHandler;

    public BotLogic(){
        this.testHandler = new TestHandler();
        this.speedTestHandler = new SpeedTestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.speedTestCommand = new SpeedTestCommand(this.speedTestHandler);
        this.keyboardService = new KeyboardService();
    }

    private static final String COMMAND_HELP =  "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

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
            if (testHandler.isTestActive(chatId)) {
                return testHandler.handleAnswer(callbackData, chatId);
            } else if (speedTestHandler.isTestActive(chatId)) {
                var result = speedTestHandler.handleAnswerWithFeedback(callbackData, chatId);
                return (String) result.get("feedback");
            } else {
                return "Сначала начните тест командой /start или /speed_test";
            }

        } else if (callbackData.equals("speed_yes_button") ||
                callbackData.equals("speed_no_button")) {
            return speedTestCommand.handleButtonClick(callbackData, chatId);
        } else if (callbackData.equals("next_button")) {
            if (speedTestHandler.isTestActive(chatId)) {
                return speedTestHandler.moveToNextQuestion(chatId);
            } else {
                return "Тест не активен";
            }
        } else {
            return startCommand.handleButtonClick(callbackData, chatId);
        }
    }

    /**
     * Если в сообщении была команда, т.е. текст начинается с /, то обрабатываем ее
     *и высылаем текст, который привязан к командам
     */
    public String handleCommand(String command) {
        switch (command) {
            case "/start":
                // StartCommand - отельный класс для реализации старта бота,в дальнейшем логично было бы на каждую задачу выводить по классу
                return startCommand.startTest();
            case "/speed_test":
                return speedTestCommand.startTest();
            case "/help":
                return COMMAND_HELP;

            default:
                return COMMAND_UNKNOWN;
        }

    }

    /**
     * handleCallbackQuery - собирает результаты обработки в список
     */
    public List<String> handleCallbackQuery(String callbackData, long chatId) {
        String responseText = processCallbackData(callbackData, chatId);
        String keyboardType = getKeyboardForCallback(callbackData, chatId);

        // возвращаем список: [chatId, responseText, keyboardType]
        List<String> result = new ArrayList<>();
        result.add(String.valueOf(chatId));
        result.add(responseText);
        result.add(keyboardType != null ? keyboardType : "");

        return result;
    }


    // обработка всех входящих сообщений
    public List<String> handleTextMessage(long chatId, String messageText) {
        List<String> result = new ArrayList<>();
            // команда из бокового меню
            if (messageText.startsWith("/")) {
                String responseText = handleCommand(messageText);
                String keyboardType = getKeyboardForCommand(messageText);

                result.add(String.valueOf(chatId));
                result.add(responseText);
                result.add(keyboardType != null ? keyboardType : "");

                System.out.println("Обработана команда из бокового меню: " + messageText);
            }
        return result;
    }
    /**
     * метод для распределения входящих данных на кнопки и текст
     */
    public List<String> processInput(String inputType, long chatId, String data) {
        if ("callback".equals(inputType)) {
            return handleCallbackQuery(data, chatId);
        } else if ("message".equals(inputType)) {
            return handleTextMessage(chatId, data);
        }

        return new ArrayList<>();
    }


    /**
     *  метод определения ключа показываемого списка кнопок после нажатия
     */
    public String getKeyboardForCallback(String callbackData, long chatId) {
        switch (callbackData) {
            case "yes_button" -> { return "test_answers"; }
            case "A_button", "B_button", "C_button", "D_button" -> {
                if (testHandler.isTestActive(chatId)) {
                    return "test_answers";
                }else if (speedTestHandler.isTestActive(chatId)){
                    return "speed_test_next";
                }
            }
            case "speed_yes_button" -> {return "test_answers";}
            case "next_button" -> {
                if (speedTestHandler.isTestActive(chatId)) {
                    return "test_answers";
                }
            }
        }
        return null;
    }

    //логика определения типа команды в боковом меню
    public String getKeyboardForCommand(String command) {
        if (command != null) {
            switch (command) {
                case "/start":
                    return "start";
                case "/speed_test":
                    return "speed_test_start";
                default:
                    return null;
            }
        }
        return null;
    }
    public KeyboardService getKeyboardService() {
        return keyboardService;
    }
}
