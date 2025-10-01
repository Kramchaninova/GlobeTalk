
package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Bot.java - класс обрабатывающий
 * Обрабатывает текстовые команды и кнопки, возвращая ответы пользователю.
 * ВАЖНО: простой текст он воспринимать никак не будет
 */

/**
 * Класс, который реализует бота, пишем весь функционал
 *
 *
 */
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final StartBot startBot;

    public Bot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        //закидываем клиенрта в стартбот
        this.startBot = new StartBot(telegramClient);
    }
    /**
     * Основный метод, в котором мы обрабатываем входящие сообщения,
     * извлекаем текст, ID, отправляем ответ с обработкой ошибок
     * добавляем обработку кнопок под текстом
     */
    @Override
    public void consume(Update update) {

        //ДОБАВЛЕНО (относительно эхо бота)
        //обаботка нажатий кнопок под текстом
        if (update.hasCallbackQuery()) { //это грубо говоря проверка на нажатие кнопки
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            SendMessage message = startBot.HandleButtonClick(callbackData, chatId);
            try {
                if (message != null) {
                    // это типо если у нас не выслалось сообщение то отправляем его пользователю
                    // то самое дружественное приветсвие
                    telegramClient.execute(message);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        //это уже чисто для команд
        else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message;

            if (messageText.startsWith("/")) {
                message = handleCommand(messageText, chatId);
            }  else {
                return;
            }
            try {
                if (message != null) {
                    telegramClient.execute(message);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Если в сообщении была команда, т.е. текст начинается с /, то обрабатываем ее
     *и высылаем текст, который привязан к командам
     */
    SendMessage handleCommand(String command, long chatId) {
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