package org.example;

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
     * Обрабатывает callback запросы от кнопок.
     */
    public BotResponse processCallback(String callbackData, long chatId) {
        String responseText = processCallbackData(callbackData, chatId);
        String keyboardType = getKeyboardForCallback(callbackData, chatId);

        return new BotResponse(chatId, responseText, keyboardType);
    }
    /**
     * Обрабатывает текстовые сообщения (в нашем случае только команды) от пользователя.
     */
    public BotResponse processMessage(String messageText, long chatId) {
        if (messageText.startsWith("/")) {
            return handleCommand(messageText, chatId);
        }
        return new BotResponse(chatId, "Сообщение получено");
    }

    /**
     * Обрабатывает команды из бокового меню.
     */
    BotResponse handleCommand(String command, long chatId) {
        String responseText;
        String keyboardType = null;

        switch (command) {
            case "/start":
                responseText = startCommand.startTest();
                keyboardType = "start";
                break;
            case "/speed_test":
                responseText = speedTestCommand.startTest();
                keyboardType = "speed_test_start";
                break;
            case "/help":
                responseText = COMMAND_HELP;
                break;
            default:
                responseText = COMMAND_UNKNOWN;
        }

        return new BotResponse(chatId, responseText, keyboardType);
    }
    /**
     * Обрабатывает данные callback запросов.
     */
    String processCallbackData(String callbackData, long chatId) {
        if (callbackData.equals("main_button")) {
            return COMMAND_HELP;
        } else if (callbackData.equals("A_button") ||
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
     * определяет тип клавиатуры для отображения после нажатия callback-кнопки.
     * @return тип клавиатуры для отображения или null, если клавиатура не требуется
     */
    public String getKeyboardForCallback(String callbackData, long chatId) {
        switch (callbackData) {
            case "yes_button" -> { return "test_answers"; }
            case "A_button", "B_button", "C_button", "D_button" -> {
                if (testHandler.isTestActive(chatId)) {
                    return "test_answers";
                }else if (speedTestHandler.isTestActive(chatId)){
                    return "speed_test_next";
                }else {
                    // Если тест завершен - показываем кнопку на главную
                    return "main";
                }
            }
            case "no_button" -> {return "main";}
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
