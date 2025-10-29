package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * KeyboardService - класс для работы с кнопками.
 * Хранит все кнопки в одном словаре.
 */
public class KeyboardService {

    // Все кнопки в одном словаре
    private final Map<String, String> allButtons = new HashMap<>();

    public KeyboardService() {
        // Кнопки старта
        allButtons.put("Конечно!", "yes_button");
        allButtons.put("Назад", "no_button");

        // Кнопки ответов на тест
        allButtons.put("A", "A_button");
        allButtons.put("B", "B_button");
        allButtons.put("C", "C_button");
        allButtons.put("D", "D_button");
    }

    /**
     * Возвращает только кнопки для бокового меню
     */
    public Map<String, String> getStartButtons() {
        Map<String, String> menuButtons = new HashMap<>();
        menuButtons.put("Конечно!", allButtons.get("Конечно!"));
        menuButtons.put("Назад", allButtons.get("Назад"));
        return menuButtons;
    }

    /**
     * Возвращает только кнопки для теста
     */
    public Map<String, String> getTestButtons() {
        Map<String, String> testButtons = new HashMap<>();
        testButtons.put("A", allButtons.get("A"));
        testButtons.put("B", allButtons.get("B"));
        testButtons.put("C", allButtons.get("C"));
        testButtons.put("D", allButtons.get("D"));
        return testButtons;
    }
}
