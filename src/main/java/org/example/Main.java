package org.example;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный класс для запуска и настройки Telegram бота

 * Реализует механизм long-polling для получения обновлений
 */
public class Main {

    /**
     * Запускает и настраивает Telegram бота с использованием long-polling
     * Создает экземпляр бота, регистрирует команды и поддерживает работу приложения
     *
     * @param botToken токен аутентификации для Telegram Bot API
     */
    public void startBot(String botToken) {
        // Использование try-with-resources для автоматического управления ресурсами
        // TelegramBotsLongPollingApplication обрабатывает входящие обновления с серверов Telegram
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {

            // Регистрация команд бота в меню Telegram
            registerBotCommands(botToken);

            // Создание экземпляра бота с предоставленным токеном
            Bot bot = new Bot(botToken);

            // Регистрация бота в приложении - завершение настройки
            botsApplication.registerBot(botToken, bot);

            // Отчет об успешной регистрации в терминале
            System.out.println("Бот работает");

            // Блокировка основного потока для поддержания работы приложения
            // Thread.currentThread() - текущий поток выполнения
            // join() - заставляет текущий поток ждать завершения другого потока
            Thread.currentThread().join();

        } catch (Exception e) {
            // Обработка исключений, возникающих во время работы бота
            e.printStackTrace();
        }
    }

    /**
     * Регистрирует команды бота в меню Telegram
     *
     * @param botToken токен аутентификации для Telegram Bot API
     */
    private void registerBotCommands(String botToken) {
        try {
            List<BotCommand> commands = new ArrayList<>();

            // Добавление команд (без символа '/', так как это формальный признак команды для бота)
            commands.add(new BotCommand("start", "начать работу с ботом"));
            commands.add(new BotCommand("help", "справка по командам"));

            // OkHttpTelegramClient - реализация клиента для взаимодействия с Telegram API
            OkHttpTelegramClient client = new OkHttpTelegramClient(botToken);

            // SetMyCommands - метод Telegram Bot API для настройки команд бота
            SetMyCommands setCommands = SetMyCommands.builder() // Создание меню команд
                    .commands(commands)          // Передача списка команд
                    .scope(new BotCommandScopeDefault()) // Область видимости (все чаты)
                    .build(); // Финальное создание объекта

            // Отправка команд на сервер Telegram (execute возвращает boolean)
            client.execute(setCommands);

            // Отчет в терминал об успешной регистрации команд
            System.out.println("Команды зарегистрированы");

        } catch (Exception e) {
            // Обработка ошибок при регистрации команд
            System.err.println("Ошибка регистрации команд: " + e.getMessage());

            // e - объект исключения
            // printStackTrace() - метод для вывода подробной информации об ошибке в консоль
            e.printStackTrace();
        }
    }

    /**
     * Главный метод приложения
     * Загружает токен бота и запускает его
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        Token token = new Token();
        token.load();
        String botToken = token.get();
        Main mainInstance = new Main();
        mainInstance.startBot(botToken);
    }
}