package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * KeyboardService - класс для работы с меню слева и кнопками
 */
public class KeyboardService {

    /**
     * создает кнопки после /start команды: "Конечно!" и "Назад"
     * @param chatId ID чата (не используется, но оставлен для совместимости)
     * @return InlineKeyboardMarkup с кнопками
     */
    public InlineKeyboardMarkup createStartButton(long chatId) {
        InlineKeyboardButton yesButton = InlineKeyboardButton.builder()
                .text("Конечно!")
                .callbackData("yes_button")
                .build();

        InlineKeyboardButton noButton = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("no_button")
                .build();

        // создается список кнопокв строке
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(yesButton);
        row.add(noButton);

        //создаем клавиатуру
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }

    /**
     * создание кнопок A, B, C, D для тестов
     * @return InlineKeyboardMarkup с кнопками ответов
     */
    public InlineKeyboardMarkup createTestAnswerKeyboard() {
        InlineKeyboardButton aButton = InlineKeyboardButton.builder()
                .text("A")
                .callbackData("A_button")
                .build();
        InlineKeyboardButton bButton = InlineKeyboardButton.builder()
                .text("B")
                .callbackData("B_button")
                .build();
        InlineKeyboardButton cButton = InlineKeyboardButton.builder()
                .text("C")
                .callbackData("C_button")
                .build();
        InlineKeyboardButton dButton = InlineKeyboardButton.builder()
                .text("D")
                .callbackData("D_button")
                .build();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(aButton);
        row.add(bButton);
        row.add(cButton);
        row.add(dButton);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }

    /**
     * метод, опеределяющий какой набор кнопок показывать в зависимости от callback
     * @param callbackData данные callback
     * @param chatId ID чата
     * @param testHandler обработчик тестов
     * @return соответствующая клавиатура или null
     */
    public InlineKeyboardMarkup getKeyboardForCallback(String callbackData, long chatId, TestHandler testHandler) {
        switch (callbackData) {
            case "yes_button" -> {
                return createTestAnswerKeyboard();
            }
            case "A_button", "B_button", "C_button", "D_button" -> {
                //проверка на активность теста
                if (testHandler.isTestActive(chatId)) {
                    return createTestAnswerKeyboard();
                } else {
                    return null;
                }
            }
            case "no_button" -> {
                return createStartButton(chatId);
            }
        }
        return null;
    }

    /**
     * определяет, нужна ли клавиатура для команды
     * @param command команда
     * @param chatId ID чата
     * @return соответствующая клавиатура или null
     */
    public InlineKeyboardMarkup getKeyboardForCommand(String command, long chatId) {
        if (command != null && command.equals("/start")) {
            return createStartButton(chatId);
        }
        return null;
    }
}