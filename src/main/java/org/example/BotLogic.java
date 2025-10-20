package org.example;


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

    private static final String COMMAND_HELP = "  **Список доступных команд:**\n\n" +
            "'/start' - начать работу с ботом\n" +
            "'/help' - показать эту справку\n" +
            "     **Как взаимодействовать с ботом:**\n" +
            "Телеграмм бот работает по принципу ввода сообщение:\n" +
            "- если сообщение начинается не '/' то он просто повторяет\n" +
            "- если же начинается с '/' то он воспринимает это как команду";


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
     *и высылаем текст, который привязан к командам
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
     * Получение клавиатуры для callback
     */
    public Object getKeyboardForCallback(String callbackData, long chatId) {
        return keyboardService.getKeyboardForCallback(callbackData, chatId, testHandler);
    }

    /**
     * Получение клавиатуры для команды
     */
    public Object getKeyboardForCommand(String command, long chatId) {
        return keyboardService.getKeyboardForCommand(command, chatId);
    }
}
