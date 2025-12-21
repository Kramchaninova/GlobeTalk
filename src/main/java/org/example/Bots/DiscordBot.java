package org.example.Bots;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
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
import org.example.SheduleMessages.DistributionService;
import org.example.SheduleMessages.UniversalDistributionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DiscordBot - основной класс бота для Discord
 * Реализует подобный интерфейс как TelegramBot с общей логикой BotLogic
 */
public class DiscordBot extends ListenerAdapter {
    private JDA jda;
    private final BotLogic botLogic;
    private final Map<String, List<Button>> buttonCache = new HashMap<>();
    private final DistributionService wordDistribution;
    private final DistributionService testDistribution;
    private final DistributionService oldWordDistribution;

    public DiscordBot(String botToken) {
        this.botLogic = new BotLogic();

        this.wordDistribution = new UniversalDistributionService(
                botLogic,
                this::sendMessageToChannel,
                "ежедневные слова",
                "discord" // ← Добавлен параметр платформы
        );

        this.testDistribution = new UniversalDistributionService(
                botLogic,
                this::sendMessageToChannel,
                "отложенные тесты",
                "discord"
        );

        this.oldWordDistribution = new UniversalDistributionService(
                botLogic,
                this::sendMessageToChannel,
                "старое слово",
                "discord"
        );

        initializeBot(botToken);
    }

    /**
     * Запускает бота с указанным токеном
     */
    public void initializeBot(String botToken) {
        try {
            this.jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .setActivity(Activity.playing("Type /help"))
                    .build();

            jda.awaitReady();
            registerBotCommands(jda);
            initializeButtons();
            startDistributions();

            System.out.println("DiscordBot запущен и готов к работе");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Запускает все рассылки, с определенными параметрами задержек
     */
    private void startDistributions() {
        wordDistribution.startDistribution(100, 3 * 60);
        testDistribution.startDistribution(150, 2 * 60);
        oldWordDistribution.startDistribution(30, 60);
        System.out.println("Все рассылки DiscordBot запущены");
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
                Commands.slash("word", "отложенные сообщения"),
                Commands.slash("scheduled_test", "отложенный тест по словам"),
                Commands.slash("old_word", "Забытое слово")
        ).queue();
    }

    /**
     * onEventReceived - получает события из Discord и передает в BotLogic
     * на подобе onUpdateReceived в TelegramBot
     */
    @Override
    public void onGenericEvent(GenericEvent genericEvent) {
        try {
            BotResponse response = null;
            long channelId = 0;

            if (genericEvent instanceof SlashCommandInteractionEvent event) {
                String commandName = "/" + event.getName();
                channelId = event.getChannel().getIdLong();
                response = botLogic.processMessage(commandName, channelId);
            } else if (genericEvent instanceof ButtonInteractionEvent event) {
                String callbackData = event.getComponentId();
                channelId = event.getChannel().getIdLong();
                response = botLogic.processCallback(callbackData, channelId);
            } else if (genericEvent instanceof MessageReceivedEvent event) {
                if (event.getAuthor().isBot()) return;
                String messageText = event.getMessage().getContentRaw();
                channelId = event.getChannel().getIdLong();
                response = botLogic.processMessage(messageText, channelId);
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
     * Универсальный метод отправки сообщения для рассылок
     * @param response данные сообщения
     * @return true если отправка успешна
     */
    private boolean sendMessageToChannel(BotResponse response) {
        try {
            if (jda == null) {
                System.err.println("JDA не инициализирован");
                return false;
            }

            var channel = jda.getTextChannelById(response.getChatId());
            if (channel == null) {
                System.err.println("Канал не найден: " + response.getChatId());
                return false;
            }

            if (response.hasKeyboard() && buttonCache.containsKey(response.getKeyboardType())) {
                channel.sendMessage(response.getText())
                        .addActionRow(buttonCache.get(response.getKeyboardType()))
                        .queue();
            } else {
                channel.sendMessage(response.getText()).queue();
            }

            return true;
        } catch (Exception e) {
            System.err.println("Ошибка отправки сообщения в Discord: " + e.getMessage());
            return false;
        }
    }

    /**
     * создание сообщения с кнопками
     * подобие createMessage в TelegramBot
     */
    private void sendMessage(GenericEvent event, BotResponse response) {
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            if (response.hasKeyboard() && buttonCache.containsKey(response.getKeyboardType())) {
                slashEvent.reply(response.getText()).addActionRow(buttonCache.get(response.getKeyboardType())).queue();
            } else {
                slashEvent.reply(response.getText()).queue();
            }
        } else if (event instanceof ButtonInteractionEvent buttonEvent) {
            if (response.hasKeyboard() && buttonCache.containsKey(response.getKeyboardType())) {
                buttonEvent.reply(response.getText()).addActionRow(buttonCache.get(response.getKeyboardType())).queue();
            } else {
                buttonEvent.reply(response.getText()).queue();
            }
        } else if (event instanceof MessageReceivedEvent messageEvent) {
            if (response.hasKeyboard() && buttonCache.containsKey(response.getKeyboardType())) {
                messageEvent.getChannel().sendMessage(response.getText())
                        .addActionRow(buttonCache.get(response.getKeyboardType()))
                        .queue();
            } else {
                messageEvent.getChannel().sendMessage(response.getText()).queue();
            }
        }
    }

    /**
     * создание наборов кнопок из map словарей
     * @return готовый набор уже сформированных кнопок для сообщения
     */
    private List<Button> createButtonsFromMap(Map<String, String> buttonConfigs) {
        return buttonConfigs.entrySet().stream()
                .map(entry -> Button.primary(entry.getValue(), entry.getKey()))
                .toList();
    }

    /**
     * создание клавиатур (набор) кнопок под определенными ключами
     */
    private void initializeButtons() {
        buttonCache.put("start", createButtonsFromMap(botLogic.getKeyboardService().getStartButtonConfigs()));
        buttonCache.put("test_answers", createButtonsFromMap(botLogic.getKeyboardService().getTestAnswerConfigs()));
        buttonCache.put("speed_test_next", createButtonsFromMap(botLogic.getKeyboardService().getSpeedTestNextButton()));
        buttonCache.put("speed_test_start", createButtonsFromMap(botLogic.getKeyboardService().getSpeedTestStartButton()));
        buttonCache.put("dictionary", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryMainButton()));
        buttonCache.put("add_again", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryAddAgainButton()));
        buttonCache.put("delete", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryDeleteButton()));
        buttonCache.put("delete_cancel", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryDeleteCancelButton()));
        buttonCache.put("dictionary_final_button", createButtonsFromMap(botLogic.getKeyboardService().getDictionaryFinalButton()));
        buttonCache.put("main", createButtonsFromMap(botLogic.getKeyboardService().getMainButtonCallBack()));
        buttonCache.put("sing_in_main", createButtonsFromMap(botLogic.getKeyboardService().getSingInMain()));
        buttonCache.put("sing_in_end", createButtonsFromMap(botLogic.getKeyboardService().getSingInEnd()));
        buttonCache.put("login_error", createButtonsFromMap(botLogic.getKeyboardService().getLoginError()));
        buttonCache.put("my_profile", createButtonsFromMap(botLogic.getKeyboardService().getMyProfile()));
        buttonCache.put("login_password_edit_end", createButtonsFromMap(botLogic.getKeyboardService().getLoginPasswordEditEnd()));
        buttonCache.put("log_out_confirm", createButtonsFromMap(botLogic.getKeyboardService().getLogOutConfirmation()));
        buttonCache.put("schedule_message", createButtonsFromMap(botLogic.getKeyboardService().getScheduleMessage()));
        buttonCache.put("schedule_message_final", createButtonsFromMap(botLogic.getKeyboardService().getScheduleMessageFinal()));
        buttonCache.put("schedule_test", createButtonsFromMap(botLogic.getKeyboardService().getScheduleTestYesOrNo()));

        System.out.println("Кнопки DiscordBot инициализированы");
    }

    /**
     * Остановка бота и рассылок
     */
    public void shutdown() {
        wordDistribution.stopDistribution();
        testDistribution.stopDistribution();
        oldWordDistribution.stopDistribution();

        if (jda != null) {
            jda.shutdown();
        }
        System.out.println("DiscordBot и все рассылки остановлены");
    }
}