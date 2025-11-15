package org.example;

import org.example.Bots.DiscordBot;
import org.example.Bots.TelegramBot;
import org.example.Tokens.TokenTelegram;
import org.example.Tokens.TokenDiscord;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Запуск и настройка ботов (Telegram и Discord).
 * Реализует механизм long-polling для получения обновлений.
 * Реализованна параллельность работ ботов
 */
public class Main {
    public static void main(String[] args) {
        // Запуск Telegram бота в отдельном потоке
        new Thread(() -> {
            try {
                TokenTelegram tokenTelegram = new TokenTelegram();
                tokenTelegram.load();
                String telegramBotToken = tokenTelegram.get();

                if (telegramBotToken == null || telegramBotToken.isEmpty()) {
                    System.err.println("ошибка с токеном Telegram");
                    return;
                }

                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(new TelegramBot(telegramBotToken, "GlobeTalk"));
                System.out.println("Telegram бот работает");
            } catch (TelegramApiException e) {
                System.err.println("ошибка запуска Telegram бота: " + e.getMessage());
            }
        }).start();

        // Запуск Discord бота в основном потоке
        TokenDiscord tokenDiscord = new TokenDiscord();
        tokenDiscord.load();
        String discordBotToken = tokenDiscord.get();

        if (discordBotToken == null || discordBotToken.isEmpty()) {
            System.err.println("ошибка с токеном Discord");
            return;
        }

        try {
            DiscordBot discordBot = new DiscordBot();
            discordBot.initializeBot(discordBotToken);
            System.out.println("Discord бот работает");
        } catch (Exception e) {
            System.err.println("ошибка запуска Discord бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}