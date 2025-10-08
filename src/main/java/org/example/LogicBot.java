package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



/** LogicBot - класс для обработки логики бота.
 * обрабатывает входящие сообщения, команды и callback запросы от кнопок
 *
 */


public class LogicBot {
    private final Bot bot;
    private final StartBot startBot;

    public LogicBot(Bot bot){
        this.bot = bot;
        this.startBot = new StartBot();
    }

    /**метод обаботки входящий обновлений (сообщений) и нажание кнопок
     * @param update
     */
    public void processUpdate(Update update) {
        try {
            //ДОБАВЛЕНО (относительно эхо бота)
            //обаботка нажатий кнопок под текстом
            if (update.hasCallbackQuery()) { //это грубо говоря проверка на нажатие кнопки
                String callbackData = update.getCallbackQuery().getData();
                long chatId = update.getCallbackQuery().getMessage().getChatId();

                SendMessage message = startBot.HandleButtonClick(callbackData, chatId);
                bot.execute(message);
            }
            //это уже чисто для команд
            else if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();


                if (messageText.startsWith("/")) {
                    SendMessage responce = handleCommand(messageText, chatId);
                    if (responce!=null) bot.execute(responce);
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
    private SendMessage handleCommand(String command, long chatId) {
        String responseText;

        switch (command) {
            case "/start":
                /** StartBot - отельный класс для реализации старта бота
                 * в дальнейшем логично было бы на каждую задачу выводить по классу
                 */
                return startBot.startTest(chatId);


            case "/help":
                responseText = "  **Список доступных команд:**\n\n" +
                        "'/start' - начать работу с ботом\n" +
                        "'/help' - показать эту справку\n" +
                        "     **Как взаимодействовать с ботом:**\n" +
                        "Телеграмм бот работает по принципу ввода сообщение:\n" +
                        "- если сообщение начинается не '/' то он просто повторяет\n" +
                        "- если же начинается с '/' то он воспринимает это как команду";
                break;

            default:
                responseText = "Неизвестная команда. Введите /help для списка доступных команд.";
                break;
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text(responseText)
                .build();
    }
}
