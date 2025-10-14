package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



/**
 * LogicBot - класс для обработки логики бота.
 * обрабатывает входящие сообщения, команды и callback запросы от кнопок
 */


public class LogicBot {
    private final Bot bot;
    private final StartBot startBot;
    private final TestManager testManager;

    public LogicBot(Bot bot){
        this.bot = bot;
        this.startBot = new StartBot();
        this.testManager = new TestManager();
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
     * processUpdate - метод обаботки входящий обновлений (сообщений) и нажание кнопок
     * @param update
     */
    public void processUpdate(Update update) {
        try {
            //ДОБАВЛЕНО (относительно эхо бота)
            //обаботка нажатий кнопок под текстом
            if (update.hasCallbackQuery()) { //это грубо говоря проверка на нажатие кнопки
                String callbackData = update.getCallbackQuery().getData();
                long chatId = update.getCallbackQuery().getMessage().getChatId();

                String responseText;

                if (callbackData.equals("A_button") ||
                        callbackData.equals("B_button") ||
                        callbackData.equals("C_button") ||
                        callbackData.equals("D_button")) {
                    responseText = testManager.handleAnswer(callbackData, chatId);
                } else {
                    responseText = startBot.handleButtonClick(callbackData, chatId);
                }


                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(responseText)
                        .build();

                //добавила реакции на сообщения и метод isTestActive, тк если этого не сделать
                //все следующии сообщения псоле кнопки yes_button будут идти с 4мя кнопками
                if (callbackData.equals("yes_button")){
                    message.setReplyMarkup(startBot.createAnswerKeyboard());
                }else if (callbackData.equals("A_button") ||
                        callbackData.equals("B_button") ||
                        callbackData.equals("C_button") ||
                        callbackData.equals("D_button")) {
                    if (TestManager.isTestActive(chatId)) {
                            message.setReplyMarkup(startBot.createAnswerKeyboard());
                        }
                } else if (callbackData.equals("no_button")) {
                    message.setReplyMarkup(startBot.createStartButton(chatId));
                }

                bot.execute(message);
            }
            //это уже чисто для команд
            else if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();


                if (messageText.startsWith("/")) {
                    String responseText = handleCommand(messageText, chatId);
                    if (responseText != null) {
                        SendMessage response = SendMessage.builder()
                                .chatId(chatId)
                                .text(responseText)
                                .build();
                        if (messageText.equals("/start")) {
                            response.setReplyMarkup(startBot.createStartButton(chatId));
                        }

                        bot.execute(response);
                    }
                }
            }
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    /**
     * Если в сообщении была команда, т.е. текст начинается с /, то обрабатываем ее
     *и высылаем текст, который привязан к командам
     */
    public String handleCommand(String command, long chatId) {
        switch (command) {
            case "/start":
                /** StartBot - отельный класс для реализации старта бота
                 * в дальнейшем логично было бы на каждую задачу выводить по классу
                 */
                return startBot.startTest(chatId);

            case "/help":
                return COMMAND_HELP;

            default:
                return COMMAND_UNKNOWN;
        }

    }
}
