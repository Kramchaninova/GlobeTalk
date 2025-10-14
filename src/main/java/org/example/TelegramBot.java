
package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


/**
 * TelegramBot.java - основной клёасс бота, реализующий интерфейс для получения обновлений
 * Обрабатывает текстовые команды и кнопки, возвращая ответы пользователю.т
 */
public class TelegramBot extends TelegramLongPollingBot  {

    private final String botToken;
    private final String botUsername;
    private final LogicBot logicBot;

    public TelegramBot(String botToken, String botUsername) {
        super(botToken); //супер вызывает конструктор родительского класс лонгполинг (выше)
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.logicBot = new LogicBot();
        registerBotCommands();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    @Override
    public void onUpdateReceived(Update update){
        // передаем все обновления в LogicBot для обработки
        logicBot.processUpdate(update, this);
    }

    /**
     * registerBotCommands - регистрирует команды бота в боковом меню Telegram
     */
    public void registerBotCommands() {
        try {
            List<BotCommand> commands = new ArrayList<>();

            // добавление команд (без символа '/', так как это формальный признак команды для бота)
            commands.add(new BotCommand("start", "начать работу с ботом"));
            commands.add(new BotCommand("help", "справка по командам"));

            //esecute - способ отправки запроса к телеграм апи
            execute(SetMyCommands.builder()
                    .commands(commands) //передача списка комаед
                    .scope(new BotCommandScopeDefault()) //scope - область видимости, а вторая после нью типо всем
                    .build());

            System.out.println("команды зарегестрированы в боковом меню");
        } catch (TelegramApiException e) {
            System.err.println("ошибка регистрации команд: " + e.getMessage());
        }
    }

}