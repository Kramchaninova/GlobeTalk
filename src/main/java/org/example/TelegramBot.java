package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

/**
 * TelegramBot - основной класс бота, реализующий получение обновлений
 */
public class TelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final BotLogic botLogic;

    private final Map<String, InlineKeyboardMarkup> keyboardCache = new HashMap<>();

    public TelegramBot(String botToken, String botUsername) {
        super(botToken);
        this.botUsername = botUsername;
        this.botLogic = new BotLogic();
        registerBotCommands();
        initializeKeyboards();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void registerBotCommands() {
        try {
            List<BotCommand> commands = new ArrayList<>();
            botLogic.getStartButtons(); // для примера
            botLogic.getTestButtons(); // для примера

            // создаём команды
            commands.add(new BotCommand("start", "начать работу с ботом"));
            commands.add(new BotCommand("help", "справка по командам"));

            execute(SetMyCommands.builder()
                    .commands(commands)
                    .scope(new BotCommandScopeDefault())
                    .build());

            System.out.println("Команды зарегистрированы в боковом меню");
        } catch (TelegramApiException e) {
            System.err.println("Ошибка регистрации команд: " + e.getMessage());
        }
    }

    private InlineKeyboardMarkup createKeyboardFromMap(Map<String, String> buttonConfigs, int buttonsPerRow) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        int i = 0;

        for (Map.Entry<String, String> entry : buttonConfigs.entrySet()) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(entry.getKey())
                    .callbackData(entry.getValue())
                    .build();
            currentRow.add(button);

            if (currentRow.size() == buttonsPerRow || i == buttonConfigs.size() - 1) {
                keyboard.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
            i++;
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }

    private void initializeKeyboards() {
        keyboardCache.put("start", createKeyboardFromMap(
                botLogic.getStartButtons(), 2));
        keyboardCache.put("test_answers", createKeyboardFromMap(
                botLogic.getTestButtons(), 4));
        System.out.println("Клавиатуры инициализированы");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            String[] result;
            long chatId;

            if (update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getMessage().getChatId();
                result = botLogic.processInput("callback", chatId, update.getCallbackQuery().getData());
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                chatId = update.getMessage().getChatId();
                result = botLogic.processInput("message", chatId, update.getMessage().getText());
            } else {
                return;
            }

            if (result != null) {
                SendMessage message = createMessage(chatId, result[0], result[1]);
                execute(message);
            }
        } catch (TelegramApiException e) {
            System.err.println("Ошибка Telegram API: " + e.getMessage());
        }
    }

    private SendMessage createMessage(long chatId, String text, String keyboardType) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();

        if (keyboardType != null && !keyboardType.isEmpty() && keyboardCache.containsKey(keyboardType)) {
            message.setReplyMarkup(keyboardCache.get(keyboardType));
        }

        return message;
    }
}
