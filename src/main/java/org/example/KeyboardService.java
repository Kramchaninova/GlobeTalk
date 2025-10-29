package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    //гетеры возвращающие копию map коллекции
    //@return map где ключ - текст кнопки, значение - callback данные
    public Map<String, String> getStartButtonConfigs() {
        return new HashMap<>(startButtons);
    }
    public Map<String, String> getTestAnswerConfigs() {
        return new HashMap<>(testAnswerButtons);
    }

    /**
     * хранит команды для бокового меню
     * @return список команд бота для бокового меню
     */
    public Map<String, String> getBotCommandsAsMap() {
        Map<String, String> commands = new HashMap<>();
        commands.put("/start", "начать работу с ботом");
        commands.put("/help", "справка по командам");
        return commands;
    }

}