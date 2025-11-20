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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DiscordBot - основной класс бота для Discord
 * Реализует подобный интерфейс как TelegramBot с общей логикой BotLogic
 */
public class DiscordBot extends ListenerAdapter {
    private final BotLogic botLogic;
    private final Map<String, List<Button>> keyboardCache = new HashMap<>();

    public DiscordBot() {
        this.botLogic = new BotLogic();
    }

    /**
     * Запускает бота с указанным токеном
     * */
    public void initializeBot(String botToken) {
        try {
            JDA jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .setActivity(Activity.playing("Type /help"))
                    .build();

            jda.awaitReady();
            registerBotCommands(jda);
            initializeButtons();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Регистрирует слеш-команды в Discord
     * @return
     */
    private void registerBotCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("start", "начать работу с ботом"),
                Commands.slash("start_test", "начальный тест на уровень знаний"),
                Commands.slash("help", "справка по командам"),
                Commands.slash("speed_test", "тест на скорость"),
                Commands.slash("dictionary", "ваш словарь")
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
     * создание наборов кнопок из map словарей
     * @return готовый набор уже сформированных кнопок для сообщения
     */
    private List<Button> createButtonsFromMap(Map<String, String> buttonConfigs) {
        return buttonConfigs.entrySet().stream()
                .map(entry -> Button.primary(entry.getValue(), entry.getKey()))
                .toList();
    }

    /**
     * создание сообщения с кнопками
     * подобие createMessage в TelegramBot
     * @return cooбщение для отправки
     */
    private void sendMessage(GenericEvent event, BotResponse response) {
        if (event instanceof SlashCommandInteractionEvent slashEvent) {
            if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
                slashEvent.reply(response.getText()).addActionRow(keyboardCache.get(response.getKeyboardType())).queue();
            } else {
                slashEvent.reply(response.getText()).queue();
            }
        } else if (event instanceof ButtonInteractionEvent buttonEvent) {
            if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
                buttonEvent.reply(response.getText()).addActionRow(keyboardCache.get(response.getKeyboardType())).queue();
            } else {
                buttonEvent.reply(response.getText()).queue();
            }
        } else if (event instanceof MessageReceivedEvent messageEvent) {
            if (response.hasKeyboard() && keyboardCache.containsKey(response.getKeyboardType())) {
                messageEvent.getChannel().sendMessage(response.getText())
                        .addActionRow(keyboardCache.get(response.getKeyboardType()))
                        .queue();
            } else {
                messageEvent.getChannel().sendMessage(response.getText()).queue();
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

        System.out.println("Клавиатуры Discord инициализированы");
    }

}