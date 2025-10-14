package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Главный класс для запуска и настройки Telegram бота
 * Реализует механизм long-polling для получения обновлений
 */
public class Main {
    public static void main(String[] args) {
        //создаем объект, выгружаем, приравниваем к строке
        Token token = new Token();
        token.load();
        String botToken = token.get();

        String botUsername = "GlobeTalk";

        if (botToken == null || botToken.isEmpty()) {
            System.err.println("ошибка с токеном");
            return;
        }

        try {
            // DefaultBotSession — это сессия для Long Polling, через которую бот получает обновления
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot(botToken, botUsername));
            System.out.println("бот работает");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}