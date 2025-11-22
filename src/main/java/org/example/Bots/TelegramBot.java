package org.example.Bots;

import org.example.BotLogic;
import org.example.Data.BotResponse;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TelegramBot - основной класс бота для Telegram
 */
public class TelegramBot extends TelegramLongPollingBot  {

    private final String botUsername;
    private final BotLogic botLogic;
    private final ScheduledExecutorService scheduler;
    private final Map<String, InlineKeyboardMarkup> keyboardCache = new HashMap<>();

    public TelegramBot(String botToken, String botUsername) {
        super(botToken);
        this.botUsername = botUsername;
        this.botLogic = new BotLogic();
        this.scheduler = Executors.newScheduledThreadPool(1);
        registerBotCommands();
        initializeKeyboards();
        startScheduling();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * хранит команды для бокового меню
     */
    public List<BotCommand> getBotCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("start", "начать работу с ботом"));
        commands.add(new BotCommand("start_test", "начальный тест на уровень знаний"));
        commands.add(new BotCommand("help", "справка по командам"));
        commands.add(new BotCommand("speed_test", "тест на скорость"));
        commands.add(new BotCommand("my_profile", "мой профиль"));
        commands.add(new BotCommand("dictionary", "ваш словарь"));
        commands.add(new BotCommand("word", "отложенные сообщения"));
        return commands;
    }

    public void registerBotCommands() {
        try {
            List<BotCommand> commands = getBotCommands();
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
     * Запускает таймер для отложенных сообщений
     */
    private void startScheduling() {
        // Запускаем таймер каждую минуту
        scheduler.scheduleAtFixedRate(
                this::sendScheduledMessagesToAllUsers,
                10, 60, TimeUnit.SECONDS // Начинаем через 10 сек, потом каждую минуту
        );
        System.out.println("Таймер отложенных сообщений запущен в TelegramBot");
    }

    /**
     * Отправляет отложенные сообщения всем активным пользователям
     */
    private void sendScheduledMessagesToAllUsers() {
        try {
            System.out.println("TelegramBot: запуск отправки отложенных сообщений");

            // Получаем активных пользователей АВТОМАТИЧЕСКИ
            List<Long> activeUsers = getActiveTelegramUsers();

            if (activeUsers.isEmpty()) {
                System.out.println("Нет активных пользователей для рассылки");
                return;
            }

            System.out.println("Найдено " + activeUsers.size() + " активных пользователей");

            // Отправляем сообщения каждому пользователю
            for (Long chatId : activeUsers) {
                try {
                    BotResponse response = botLogic.generateScheduledMessage(chatId);
                    if (response != null && response.isValid()) {
                        // отпрвка
                        SendMessage message = createMessage(response);
                        execute(message);
                        System.out.println("Telegram: отложенное сообщение отправлено пользователю " + chatId);

                        // Небольшая задержка чтобы не превысить лимиты Telegram
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка отправки пользователю " + chatId + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка в процессе рассылки Telegram: " + e.getMessage());
        }
    }

    /**
     * Получает активных Telegram пользователей АВТОМАТИЧЕСКИ
     */
    private List<Long> getActiveTelegramUsers() {
        List<Long> users = botLogic.getActiveUsersForDistribution();

        System.out.println("Найдено " + users.size() + " активных пользователей");

        if (users.isEmpty()) {
            System.out.println("Пользователи автоматически добавляются при первом сообщении боту");
        }

        return users;
    }

    /**
     * создание набора кнопок
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
    public void onUpdateReceived(Update update) {
        try {
            BotResponse response = null;
            long chatId;

            if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                chatId = update.getCallbackQuery().getMessage().getChatId();
                response = botLogic.processCallback(callbackData, chatId);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                chatId = update.getMessage().getChatId();
                response = botLogic.processMessage(messageText, chatId);
            }

            if (response != null && response.isValid()) {
                SendMessage message = createMessage(response);
                execute(message);
            }
        } catch (TelegramApiException e) {
            System.err.println("Ошибка Telegram API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * создание сообщения с кнопками
     */
    private SendMessage createMessage(BotResponse response) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(response.getChatId()))
                .text(response.getText())
                .parseMode("Markdown") //разметка, типо: жирный, курсиввный и тд
                .build();

        if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
            message.setReplyMarkup(keyboardCache.get(response.getKeyboardType()));
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
        keyboardCache.put("dictionary", createKeyboardFromMap(
                botLogic.getKeyboardService().getDictionaryMainButton(),4));
        keyboardCache.put("add_again", createKeyboardFromMap(
                botLogic.getKeyboardService().getDictionaryAddAgainButton(), 2));
        keyboardCache.put("delete", createKeyboardFromMap(
                botLogic.getKeyboardService().getDictionaryDeleteButton(), 2));
        keyboardCache.put("delete_cancel", createKeyboardFromMap(
                botLogic.getKeyboardService().getDictionaryDeleteCancelButton(), 3));
        keyboardCache.put("dictionary_final_button", createKeyboardFromMap(
                botLogic.getKeyboardService().getDictionaryFinalButton(), 2));
        keyboardCache.put("main", createKeyboardFromMap(
                botLogic.getKeyboardService().getMainButtonCallBack(),1));
        keyboardCache.put("sing_in_main", createKeyboardFromMap(
                botLogic.getKeyboardService().getSingInMain(), 2));
        keyboardCache.put("sing_in_end", createKeyboardFromMap(
                botLogic.getKeyboardService().getSingInEnd(),2));
        keyboardCache.put("login_error", createKeyboardFromMap(
                botLogic.getKeyboardService().getLoginError(),2));
        keyboardCache.put("my_profile", createKeyboardFromMap(
                botLogic.getKeyboardService().getMyProfile(), 3));
        keyboardCache.put("login_password_edit_end", createKeyboardFromMap(
                botLogic.getKeyboardService().getLoginPasswordEditEnd(), 2));
        keyboardCache.put("log_out_confirm", createKeyboardFromMap(
                botLogic.getKeyboardService().getLogOutConfirmation(), 2));
        keyboardCache.put("schedule_message", createKeyboardFromMap(
                botLogic.getKeyboardService().getScheduleMessage(), 2));
        keyboardCache.put("schedule_message_final", createKeyboardFromMap(
                botLogic.getKeyboardService().getScheduleMessageFinal(), 2));
        System.out.println("Клавиатуры инициализированы");
    }

    /**
     * Остановка таймера
     */
    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            System.out.println("Таймер TelegramBot остановлен");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}