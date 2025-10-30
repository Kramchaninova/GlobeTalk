package org.example;

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

    //храним кнопку на главнуб (у нас главная будет - help, потому что вся содержательная информацяи там)
    private final Map<String, String> mainButtonCallBack = Map.of(
            "На Главную", "main_button"
    );

    //гетеры возвращающие копию map коллекции
    //@return map где ключ - текст кнопки, значение - callback данные
    public Map<String, String> getStartButtonConfigs() {
        return new HashMap<>(startButtons);
    }
    public Map<String, String> getTestAnswerConfigs() {
        return new HashMap<>(testAnswerButtons);
    }
    public Map<String, String> getMainButtonCallBack(){ return  new HashMap<>(mainButtonCallBack);}

}