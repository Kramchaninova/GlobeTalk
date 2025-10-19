package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



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
     * processUpdate - метод обаботки входящий обновлений (сообщений)
     */
    public void processUpdate(Update update, TelegramLongPollingBot bot) {
        try {
            //ДОБАВЛЕНО (относительно эхо бота)
            //обаботка нажатий кнопок под текстом
            if (update.hasCallbackQuery()) { //это грубо говоря проверка на нажатие кнопки
                handleCallbackQuery(update, bot);
            } else if (update.hasMessage() && update.getMessage().hasText()){
                handleMessage(update, bot);
            }
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    /**
     * handleCallbackQuery  - обработка нажатий кнопок
     */

    public void handleCallbackQuery (Update update, TelegramLongPollingBot bot)  throws TelegramApiException{
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        String responseText = processCallbackData(callbackData, chatId);
        SendMessage message = createMessageWithKeyboard(chatId, responseText, callbackData);

        bot.execute(message);
    }

    /**
     * методо обработки команд вручную введенных
     */

    private void handleMessage(Update update, TelegramLongPollingBot bot) throws TelegramApiException {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/")) {
            String responseText = handleCommand(messageText);
            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text(responseText)
                    .build();

            var keyboard = keyboardService.getKeyboardForCommand(messageText, chatId);
            if (keyboard != null) {
                response.setReplyMarkup(keyboard);
            }
            bot.execute(response);
        }
    }

    /**
     * Обработка ответов с кнопок
     */
    private String processCallbackData(String callbackData, long chatId) {
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
     * Cоздание сообщений с нужными кнопками
     */
    private SendMessage createMessageWithKeyboard(long chatId, String text, String callbackData) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        var keyboard = keyboardService.getKeyboardForCallback(callbackData, chatId, testHandler);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }

        return message;
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
}
