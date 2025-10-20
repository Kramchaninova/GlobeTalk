package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

/**
 * TelegramBotLogic - реализация логики бота
 * содержит телеграм зависимости
 */

public class TelegramBotLogic {
    private final BotLogic botLogic;
    private final TelegramLongPollingBot bot;

    public TelegramBotLogic(TelegramLongPollingBot bot) {
        this.botLogic = new BotLogic();
        this.bot = bot;
    }

    /**
     * processUpdate - метод обработки входящих обновлений (сообщений)
     */
    public void processUpdate(Update update) {
        try {
            // Обработка нажатий кнопок под текстом
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            } else if (update.hasMessage() && update.getMessage().hasText()){
                handleMessage(update);
            }
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    /**
     * handleCallbackQuery - обработка нажатий кнопок
     */
    public void handleCallbackQuery(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        // Используем основную логику из BotLogic
        String responseText = botLogic.processCallbackData(callbackData, chatId);
        SendMessage message = createMessageWithKeyboard(chatId, responseText, callbackData);

        bot.execute(message);
    }

    /**
     * метод обработки команд вручную введенных
     */
    private void handleMessage(Update update) throws TelegramApiException {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/")) {
            // Используем основную логику из BotLogic
            String responseText = botLogic.handleCommand(messageText);
            SendMessage response = SendMessage.builder()
                    .chatId(chatId)
                    .text(responseText)
                    .build();

            // Используем основную логику из BotLogic
            Object keyboard = botLogic.getKeyboardForCommand(messageText, chatId);
            if (keyboard != null) {
                response.setReplyMarkup((InlineKeyboardMarkup) keyboard);
            }
            bot.execute(response);
        }
    }

    /**
     * создание сообщений с нужными кнопками
     */
    private SendMessage createMessageWithKeyboard(long chatId, String text, String callbackData) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        // Используем основную логику из BotLogic
        Object keyboard = botLogic.getKeyboardForCallback(callbackData, chatId);
        if (keyboard != null) {
            message.setReplyMarkup((InlineKeyboardMarkup) keyboard);
        }

        return message;
    }
}