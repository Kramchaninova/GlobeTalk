package org.example;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

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
    public void startBotInTg(String botToken) {
        // Использование try-with-resources для автоматического управления ресурсами
        // TelegramBotsLongPollingApplication обрабатывает входящие обновления с серверов Telegram
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {

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
        //запуск бота
        mainInstance.startBotInTg(botToken);



    }
}