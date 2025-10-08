
package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;


/**
 * Bot.java - основной класс бота, реализующий интерфейс для получения обновлений
 * Обрабатывает текстовые команды и кнопки, возвращая ответы пользователю.т
 */
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final LogicBot logicBot;

    public Bot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.logicBot = new LogicBot(botToken);
        registerBotCommands();
    }

    @Override
    public void consume(Update update){
        // передаем все обновления в LogicBot для обработки
        logicBot.processUpdate(update);
    }

    /**
     * Регистрирует команды бота в меню Telegram, боковое меню
     */
    public void registerBotCommands() {
        try {
            List<BotCommand> commands = new ArrayList<>();

            // Добавление команд (без символа '/', так как это формальный признак команды для бота)
            commands.add(new BotCommand("start", "начать работу с ботом"));
            commands.add(new BotCommand("help", "справка по командам"));


            // SetMyCommands - метод Telegram Bot API для настройки команд бота
            SetMyCommands setCommands = SetMyCommands.builder() // Создание меню команд
                    .commands(commands)          // Передача списка команд
                    .scope(new BotCommandScopeDefault()) // Область видимости (все чаты)
                    .build(); // Финальное создание объекта

            //Проверка на отправку запроса
            boolean success = telegramClient.execute(setCommands);

            if (success) {
                System.out.println("Команды бота успешно зарегистрированы");
            } else {
                System.err.println("Не удалось зарегистрировать команды бота");
            }

        } catch (Exception e) {
            System.err.println("Ошибка регистрации команд: " + e.getMessage());
            e.printStackTrace();
        }
    }
}