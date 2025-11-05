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


    //словарь кнопок дял словаря (главной словаря)
    private final Map<String, String> dictionaryMainButton = Map.of(
            "Добавить", "dictionary_add_button",
            "Редактировать", "dictionary_edit_button",
            "Удалить", "dictionary_delete_button",
            "Назад", "main_button"
    );

    //набор кнопок для повторного добавления в словарь
    private final Map<String, String> dictionaryAddAgainButton = Map.of(
            "Да", "dictionary_add_yes_button",
            "Нет", "dictionary_add_no_button"
    );

    //набор кнопко подтверждение на удаление
    private  final Map<String, String> dictionaryDeleteButton = Map.of(
            "Подтвердить", "dictionary_delete_confirm_button",
            "Отменить",  "dictionary_delete_cancel_button"
    );

    // нобор кнопок в случае отказа от удаления
    private final Map<String, String> dictionaryDeleteCancelButton = Map.of(
            "Продлолжить", "dictionary_delete_resume_button",
            "На главную", "main_button",
            "Словарь", "dictionary_button"
    );

    //для окончаний каких то команд, чтобы было логическое завершение набор
    private final  Map<String, String> dictionaryFinalButton = Map.of(
            "Словарь", "dictionary_button",
            "На главную", "main_button"
    );



    //гетеры возвращающие копию map коллекции
    //@return map где ключ - текст кнопки, значение - callback данные
    public Map<String, String> getStartButtonConfigs() {return new HashMap<>(startButtons);}
    public Map<String, String> getTestAnswerConfigs() {return new HashMap<>(testAnswerButtons);}
    public Map<String, String> getSpeedTestStartButton() {return new HashMap<>(speedTestStartButton);}
    public Map<String, String> getSpeedTestNextButton() {return new HashMap<>(speedNextButton);}
    public Map<String, String> getDictionaryMainButton(){return  new HashMap<>(dictionaryMainButton);}
    public Map<String, String> getDictionaryAddAgainButton(){return new HashMap<>(dictionaryAddAgainButton);}
    public Map<String, String> getDictionaryDeleteButton(){return new HashMap<>(dictionaryDeleteButton);}
    public Map<String, String> getDictionaryDeleteCancelButton(){return new HashMap<>(dictionaryDeleteCancelButton);}
    public Map<String, String> getDictionaryFinalButton() {return new HashMap<>(dictionaryFinalButton);}

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
        commands.add(new BotCommand("dictionary", "ваш словарь"));


        System.out.println("команды зарегестрированы в боковом меню");
        return commands;

    }

}