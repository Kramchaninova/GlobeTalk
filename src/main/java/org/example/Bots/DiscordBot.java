package org.example.Bots;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.example.BotLogic;
import org.example.Data.BotResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DiscordBot - основной класс бота для Discord
 */
public class DiscordBot extends ListenerAdapter {
    private final BotLogic botLogic;
    private final ScheduledExecutorService scheduler;
    private final Map<String, List<Button>> keyboardCache = new HashMap<>();
    private JDA jda;

    public DiscordBot() {
        this.botLogic = new BotLogic();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Запускает бота с указанным токеном
     */
    public void initializeBot(String botToken) {
        try {
            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGES
                    )
                    .addEventListeners(this)
                    .setActivity(Activity.playing("Type /help"))
                    .build();

            jda.awaitReady();
            registerBotCommands(jda);
            initializeButtons();
            startScheduling();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Запускает таймер для отложенных сообщений
     */
    private void startScheduling() {
        scheduler.scheduleAtFixedRate(
                this::sendScheduledMessagesToAllUsers,
                10, 60, TimeUnit.SECONDS
        );
        System.out.println("Таймер отложенных сообщений запущен в DiscordBot");
    }

    /**
     * Отправляет отложенные сообщения всем активным пользователям
     */
    private void sendScheduledMessagesToAllUsers() {
        try {
            System.out.println("DiscordBot: запуск отправки отложенных сообщений");

            List<Long> activeUsers = getActiveDiscordUsers();

            if (activeUsers.isEmpty()) {
                System.out.println("Нет активных пользователей для рассылки");
                return;
            }

            System.out.println("Найдено " + activeUsers.size() + " активных пользователей");

            for (Long userId : activeUsers) {
                try {
                    BotResponse response = botLogic.generateScheduledMessage(userId);
                    if (response != null && response.isValid()) {
                        sendDirectMessage(response);
                        System.out.println("Discord: отложенное сообщение отправлено пользователю " + userId);
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка отправки пользователю " + userId + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка в процессе рассылки Discord: " + e.getMessage());
        }
    }

    /**
     * Получает активных Discord пользователей
     */
    private List<Long> getActiveDiscordUsers() {
        List<Long> users = botLogic.getActiveUsersForDistribution();
        System.out.println("Найдено " + users.size() + " активных пользователей Discord");
        return users;
    }

    /**
     * Прямая отправка сообщения пользователю в Discord
     */
    private void sendDirectMessage(BotResponse response) {
        try {
            User user = jda.retrieveUserById(response.getChatId()).complete();
            if (user != null) {
                PrivateChannel privateChannel = user.openPrivateChannel().complete();

                if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
                    privateChannel.sendMessage(response.getText())
                            .addActionRow(keyboardCache.get(response.getKeyboardType()))
                            .queue();
                } else {
                    privateChannel.sendMessage(response.getText()).queue();
                }
            } else {
                System.err.println("Пользователь с ID " + response.getChatId() + " не найден");
            }
        } catch (Exception e) {
            System.err.println("Ошибка отправки сообщения пользователю " + response.getChatId() + ": " + e.getMessage());
        }
    }

    /**
     * Регистрирует слеш-команды в Discord
     */
    private void registerBotCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("start", "начать работу с ботом"),
                Commands.slash("start_test", "начальный тест на уровень знаний"),
                Commands.slash("help", "справка по командам"),
                Commands.slash("speed_test", "тест на скорость"),
                Commands.slash("my_profile", "мой профиль"),
                Commands.slash("dictionary", "ваш словарь"),
                Commands.slash("word", "отложенные сообщения")
        ).queue();
    }

    /**
     * onEventReceived - получает события из Discord и передает в BotLogic
     */
    @Override
    public void onGenericEvent(GenericEvent genericEvent) {
        try {
            BotResponse response = null;
            long userId = 0;

            if (genericEvent instanceof SlashCommandInteractionEvent event) {
                String commandName = "/" + event.getName();
                userId = event.getUser().getIdLong();
                response = botLogic.processMessage(commandName, userId);
            } else if (genericEvent instanceof ButtonInteractionEvent event) {
                String callbackData = event.getComponentId();
                userId = event.getUser().getIdLong();
                response = botLogic.processCallback(callbackData, userId);
            } else if (genericEvent instanceof MessageReceivedEvent event) {
                if (event.getAuthor().isBot()) return;
                String messageText = event.getMessage().getContentRaw();
                userId = event.getAuthor().getIdLong();
                response = botLogic.processMessage(messageText, userId);
            }

            if (response != null && response.isValid()) {
                sendMessage(genericEvent, response);
            }
        } catch (Exception e) {
            System.err.println("Ошибка Discord API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * создание набора кнопок
     */
    private List<Button> createButtonsFromMap(Map<String, String> buttonConfigs) {
        return buttonConfigs.entrySet().stream()
                .map(entry -> Button.primary(entry.getValue(), entry.getKey()))
                .toList();
    }

    /**
     * создание сообщения с кнопками
     */
    private void sendMessage(GenericEvent event, BotResponse response) {
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
                slashEvent.reply(response.getText())
                        .addActionRow(keyboardCache.get(response.getKeyboardType()))
                        .setEphemeral(true)
                        .queue();
            } else {
                slashEvent.reply(response.getText())
                        .setEphemeral(true)
                        .queue();
            }
        } else if (event instanceof ButtonInteractionEvent buttonEvent) {
            if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
                buttonEvent.reply(response.getText())
                        .addActionRow(keyboardCache.get(response.getKeyboardType()))
                        .setEphemeral(true)
                        .queue();
            } else {
                buttonEvent.reply(response.getText())
                        .setEphemeral(true)
                        .queue();
            }
        } else if (event instanceof MessageReceivedEvent messageEvent) {
            // Для текстовых сообщений отправляем в ЛС
            try {
                User user = messageEvent.getAuthor();
                PrivateChannel privateChannel = user.openPrivateChannel().complete();

                if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
                    privateChannel.sendMessage(response.getText())
                            .addActionRow(keyboardCache.get(response.getKeyboardType()))
                            .queue();
                } else {
                    privateChannel.sendMessage(response.getText()).queue();
                }
            } catch (Exception e) {
                System.err.println("Ошибка отправки ЛС: " + e.getMessage());
            }
        }
    }

    /**
     * создание клавиатур (набор) кнопок под определенными ключами
     */
    private void initializeButtons() {
        keyboardCache.put("start", createButtonsFromMap(botLogic.getKeyboardService().getStartButtonConfigs()));
        keyboardCache.put("test_answers", createButtonsFromMap(botLogic.getKeyboardService().getTestAnswerConfigs()));
        keyboardCache.put("speed_test_next", createButtonsFromMap(botLogic.getKeyboardService().getSpeedTestNextButton()));
        keyboardCache.put("speed_test_start", createButtonsFromMap(botLogic.getKeyboardService().getSpeedTestStartButton()));
        keyboardCache.put("dictionary", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryMainButton()));
        keyboardCache.put("add_again", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryAddAgainButton()));
        keyboardCache.put("delete", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryDeleteButton()));
        keyboardCache.put("delete_cancel", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryDeleteCancelButton()));
        keyboardCache.put("dictionary_final_button", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryFinalButton()));
        keyboardCache.put("main", createButtonsFromMap(botLogic.getKeyboardService().getMainButtonCallBack()));
        keyboardCache.put("sing_in_main", createButtonsFromMap(botLogic.getKeyboardService().getSingInMain()));
        keyboardCache.put("sing_in_end", createButtonsFromMap(botLogic.getKeyboardService().getSingInEnd()));
        keyboardCache.put("login_error", createButtonsFromMap(botLogic.getKeyboardService().getLoginError()));
        keyboardCache.put("my_profile", createButtonsFromMap(botLogic.getKeyboardService().getMyProfile()));
        keyboardCache.put("login_password_edit_end", createButtonsFromMap(botLogic.getKeyboardService().getLoginPasswordEditEnd()));
        keyboardCache.put("log_out_confirm", createButtonsFromMap(botLogic.getKeyboardService().getLogOutConfirmation()));
        keyboardCache.put("schedule_message", createButtonsFromMap(botLogic.getKeyboardService().getScheduleMessage()));
        keyboardCache.put("schedule_message_final", createButtonsFromMap(botLogic.getKeyboardService().getScheduleMessageFinal()));
        System.out.println("Клавиатуры Discord инициализированы");
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
            System.out.println("Таймер DiscordBot остановлен");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}