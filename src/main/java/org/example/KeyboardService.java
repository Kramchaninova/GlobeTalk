package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * KeyboardService - класс для работы с меню и кнопками
 */
public class KeyboardService {

    /**
     * Кнопки для стартовых сообщений (/start)
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

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(yesButton);
        row.add(noButton);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }

    /**
     * Кнопки для команды /speed_test (Да/Нет)
     */
    public InlineKeyboardMarkup createSpeedTestStartButton(long chatId) {
        InlineKeyboardButton yesButton = InlineKeyboardButton.builder()
                .text("Да")
                .callbackData("speed_yes_button")
                .build();

        InlineKeyboardButton noButton = InlineKeyboardButton.builder()
                .text("Нет")
                .callbackData("speed_no_button")
                .build();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(yesButton);
        row.add(noButton);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }

    /**
     * Кнопки для тестов A/B/C/D
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
     * Метод определяет, какой набор кнопок показывать в зависимости от callback
     */
    public InlineKeyboardMarkup getKeyboardForCallback(String callbackData, long chatId, TestHandler testHandler) {
        switch (callbackData) {
            case "yes_button":
                return createTestAnswerKeyboard();
            case "A_button", "B_button", "C_button", "D_button":
                if (testHandler.isTestActive(chatId)) {
                    return createTestAnswerKeyboard();
                } else {
                    return null;
                }
            case "no_button":
                return createStartButton(chatId);
            case "speed_yes_button", "speed_no_button":
                // если тест на скорость уже начат, показываем клавиатуру A/B/C/D
                if (testHandler.isTestActive(chatId)) {
                    return createTestAnswerKeyboard();
                } else {
                    return createSpeedTestStartButton(chatId);
                }
        }
        return null;
    }

    /**
     * Определяет, нужна ли клавиатура для команды
     */
    public InlineKeyboardMarkup getKeyboardForCommand(String command, long chatId) {
        if (command != null) {
            switch (command) {
                case "/start":
                    return createStartButton(chatId);
                case "/speed_test":
                    return createSpeedTestStartButton(chatId);
            }
        }
        return null;
    }
}
