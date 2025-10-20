package org.example;

public class BotLogic {
    private final StartCommand startCommand;
    private final TestHandler testHandler;
    private final KeyboardService keyboardService;
    private final SpeedTestCommand speedTestCommand; // добавили объект
    private final SpeedTestHandler speedTestHandler;

    public BotLogic() {
        this.testHandler = new TestHandler();
        this.speedTestHandler = new SpeedTestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.keyboardService = new KeyboardService();
        this.speedTestCommand = new SpeedTestCommand(this.speedTestHandler);
    }

    private static final String COMMAND_HELP = "  **Список доступных команд:**\n\n" +
            "'/start' - начать работу с ботом\n" +
            "'/speed_test' - пройти тест на скорость\n" +
            "'/help' - показать эту справку\n" +
            "     **Как взаимодействовать с ботом:**\n" +
            "Телеграмм бот работает по принципу ввода сообщения:\n" +
            "- если сообщение начинается не '/' то он просто повторяет\n" +
            "- если же начинается с '/' то он воспринимает это как команду";

    private static final String COMMAND_UNKNOWN = "Неизвестная команда. Введите /help для списка доступных команд.";

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

    public String handleCommand(String command) {
        switch (command) {
            case "/start":
                return startCommand.startTest();
            case "/speed_test":
                return speedTestCommand.startTest();
            case "/help":
                return COMMAND_HELP;
            default:
                return COMMAND_UNKNOWN;
        }
    }

    public Object getKeyboardForCallback(String callbackData, long chatId) {
        return keyboardService.getKeyboardForCallback(callbackData, chatId, testHandler, speedTestHandler);
    }

    public Object getKeyboardForCommand(String command) {
        return keyboardService.getKeyboardForCommand(command);
    }
}
