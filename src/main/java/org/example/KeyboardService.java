package org.example;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KeyboardService - класс для работы с меню слева и кнопками
 */
public class KeyboardService {

    // храним название кнопки и callback для старта в виде словаря
    private final Map<String, String> startButtons = Map.of(
            "Конечно!", "yes_button",
            "Назад", "no_button"
    );

    // храним название кнопки и callback для ответов на тесты в виде словаря
    private final Map<String, String> testAnswerButtons = Map.of(
            "A", "A_button",
            "B", "B_button",
            "C", "C_button",
            "D", "D_button"
    );

    //храним название кнопок и callback для старта в спид тесте
    private final Map<String, String> speedTestStartButton = Map.of(
            "Да", "speed_yes_button",
            "Нет", "speed_no_button"
    );

    //храним название кнопок и callback для кнопки дальше
    private final Map<String, String> speedNextButton = Map.of(
            "Дальше", "next_button"
    );

    //гетеры возвращающие копию map коллекции
    //@return map где ключ - текст кнопки, значение - callback данные
    public Map<String, String> getStartButtonConfigs() {return new HashMap<>(startButtons);}
    public Map<String, String> getTestAnswerConfigs() {return new HashMap<>(testAnswerButtons);}
    public Map<String, String> getSpeedTestStartButton() {return new HashMap<>(speedTestStartButton);}
    public Map<String, String> getSpeedTestNextButton() {return new HashMap<>(speedNextButton);}

    /**
     * хранит команды для бокового меню
     * @return список команд бота для бокового меню
     */
    public List<BotCommand> getBotCommands() {
        List<BotCommand> commands = new ArrayList<>();

        // добавление команд (без символа '/', так как это формальный признак команды для бота)
        commands.add(new BotCommand("start", "начать работу с ботом"));
        commands.add(new BotCommand("help", "справка по командам"));
        commands.add(new BotCommand("speed_test", "тест на скорость"));


        System.out.println("команды зарегестрированы в боковом меню");
        return commands;

    }

}