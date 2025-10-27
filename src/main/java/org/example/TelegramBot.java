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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bot.java - основной класс бота, реализующий интерфейс для получения обновлений
 * Формирует текстовые (в боковом меню) команды и кнопки
 */
public class TelegramBot extends TelegramLongPollingBot  {

    private final String botUsername;
    private final BotLogic botLogic;
    private final KeyboardService keyboardService;

    private final Map<String, InlineKeyboardMarkup> keyboardCache = new HashMap<>();

    public TelegramBot(String botToken, String botUsername) {
        super(botToken); //супер вызывает конструктор родительского класс лонгполинг (выше)
        this.botUsername = botUsername;
        this.botLogic = new BotLogic();
        this.keyboardService = new KeyboardService();
        registerBotCommands();
        initializeKeyboards();

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    //registerBotCommands - формирует из списка метода getBotCommands и вызывает в команды
    public void registerBotCommands() {
        try {
            //список команд из класса для хранения кнопок и команд
            List<BotCommand> commands = keyboardService.getBotCommands();

            execute(SetMyCommands.builder()
                    .commands(commands)
                    .scope(new BotCommandScopeDefault())
                    .build());

            System.out.println("Команды зарегистрированы в боковом меню");
        } catch (TelegramApiException e) {
            System.err.println("Ошибка регистрации команд: " + e.getMessage());
        }
    }

    /**
     * создание наборов кнопок из map словарей
     * @return готовый набор уже сформированных кнопок для сообщения
     */
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

    /**
     * onUpdateReceived - получает обновления из телеграма и передает в BotLogic
     */
    @Override
    public void onUpdateReceived(Update update){
        try {
            // передаем обновление в BotLogic для обработки
            List<String> result;
            //если нажатие на кнопку произошло
            if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                long chatId = update.getCallbackQuery().getMessage().getChatId();
                result = botLogic.processInput("callback", chatId, callbackData);
                //если сообщение
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                result = botLogic.processInput("message", chatId, messageText);
            } else {
                return;
            }

            // если есть результат, создаем и отправляем сообщение
            if (!result.isEmpty()) {
                long chatId = Long.parseLong(result.get(0));
                String responseText = result.get(1);
                String keyboardType = result.get(2);

                SendMessage message = createMessage(chatId, responseText, keyboardType);
                execute(message);
            }
        } catch (TelegramApiException e){
            System.err.println("Ошибки Telegram API: нет соединения, невалидный токен, и тд." + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * создание сообщения с кнопками
     * @return cooбщение для отправки
     */
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

    /**
     * создание клавиатур (набор) кнопок под определенными ключами
     */
    private void initializeKeyboards() {
        keyboardCache.put("start", createKeyboardFromMap(
                botLogic.getKeyboardService().getStartButtonConfigs(), 2));
        keyboardCache.put("test_answers", createKeyboardFromMap(
                botLogic.getKeyboardService().getTestAnswerConfigs(), 4));
        keyboardCache.put("speed_test_next", createKeyboardFromMap(
                botLogic.getKeyboardService().getSpeedTestNextButton(), 1));
        keyboardCache.put("speed_test_start", createKeyboardFromMap(
                botLogic.getKeyboardService().getSpeedTestStartButton(), 2));
        System.out.println("Клавиатуры инициализированы");
    }


}