package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Отвечает за обработку входящих сообщений и callback-запросов.
 * Отправляет команды классу StartCommand и обработку тестов классу TestHandler.
 */

public class BotLogic {
    private final StartCommand startCommand;
    private final TestHandler testHandler;

    public BotLogic(){
        this.testHandler = new TestHandler();
        this.startCommand = new StartCommand(testHandler);
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
     * Обработка входящего обновления (сообщения) от Telegram.
     * Разделяет сообщения и callback-запросы на соответствующие обработчики.
     * @param update
     * @param bot
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
     * Обработка нажатий на inline-кнопки.
     * @param update
     * @param bot
     * @throws TelegramApiException
     */

    public void handleCallbackQuery (Update update, TelegramLongPollingBot bot)  throws TelegramApiException{
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        String responseText = processCallbackData(callbackData, chatId);
        SendMessage message = createMessageWithKeyboard(chatId, responseText, callbackData);

        bot.execute(message);
    }

    /**
     * Обработка входящих текстовых сообщений.
     * Проверяет, является ли сообщение командой (начинается с "/") и вызывает соответствующую обработку.
     * @param update
     * @param bot
     * @throws TelegramApiException
     */

    private void handleMessage(Update update, TelegramLongPollingBot bot) throws TelegramApiException {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/")) {
            String responseText = handleCommand(messageText, chatId);
            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text(responseText)
                    .build();

            if (messageText.equals("/start")) {
                response.setReplyMarkup(startCommand.createStartButton(chatId));
            }
            bot.execute(response);
        }
    }

    /**
     * Обрабатывает callbackData, полученные от нажатий кнопок.
     * Отправляет ответы на тесты TestHandler или стартовые кнопки StartCommand.
     * @param callbackData
     * @param chatId
     * @return
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
     * Создает сообщение с нужной inline-клавиатурой в зависимости от callbackData.
     * @param chatId
     * @param text
     * @param callbackData
     * @return
     */
    private SendMessage createMessageWithKeyboard(long chatId, String text, String callbackData) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        if (callbackData.equals("yes_button")) {
            message.setReplyMarkup(testHandler.createAnswerKeyboard());
        } else if (callbackData.equals("A_button") ||
                callbackData.equals("B_button") ||
                callbackData.equals("C_button") ||
                callbackData.equals("D_button")) {
            if (testHandler.isTestActive(chatId)) {
                message.setReplyMarkup(testHandler.createAnswerKeyboard());
            }
        } else if (callbackData.equals("no_button")) {
            message.setReplyMarkup(startCommand.createStartButton(chatId));
        }

        return message;
    }

    /**
     * Eсли в сообщении была команда, т.е. текст начинается с /, то обрабатываем ее.
     * Высылаем текст, который привязан к командам.
     * @param command
     * @param chatId
     * @return
     */

    public String handleCommand(String command, long chatId) {
        switch (command) {
            case "/start":
                return startCommand.startTest(chatId);

            case "/help":
                return COMMAND_HELP;

            default:
                return COMMAND_UNKNOWN;
        }

    }
}
