package org.example;

public class BotLogic {
    private final StartCommand startCommand;
    private final TestHandler testHandler;
    private final KeyboardService keyboardService;
    private final SpeedTestCommand speedTestCommand; // добавили объект

    public BotLogic() {
        this.testHandler = new TestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.keyboardService = new KeyboardService();
        this.speedTestCommand = new SpeedTestCommand(this.testHandler); // передаем testHandler
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
            return testHandler.handleAnswer(callbackData, chatId);
        } else if (callbackData.equals("speed_yes_button") ||
                callbackData.equals("speed_no_button")) {
            return speedTestCommand.handleButtonClick(callbackData, chatId);
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
        return keyboardService.getKeyboardForCallback(callbackData, chatId, testHandler);
    }

    public Object getKeyboardForCommand(String command, long chatId) {
        if (command.equals("/speed_test")) {
            return keyboardService.getKeyboardForCommand("/speed_test", chatId);
        }
        return keyboardService.getKeyboardForCommand(command, chatId);
    }
}
