package org.example;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

/**
 * Обработка команды /start и связанных кнопок.
 * Отвечает за приветственное сообщение, создание стартовой клавиатуры и обработку нажатий
 * на кнопки "Да" и "Нет" для начала теста.
 */

public class StartCommand {

    private final TestHandler testHandler;

    public StartCommand(TestHandler testHandler) {
        this.testHandler = testHandler;
    }

    private static final String START_MESSAGE = "Вас приветствует телеграмм бот GlobeTalk для изучения иностранных языков!\n\n" +
            "Перед началом обучения, пройдите короткий тестик для определения вашего уровня владения языка.\n\n" +
            "Для списка команд нажмите /help.\n\n" +
            "Вы готовы начать тест?\n\n";

    private static final String NO_BUTTON_CLICK = "Не сомневайтесь в себе!!!\n\n" +
            "Когда будете готовы используйте /start.\n\n" +
            "Для списка команд нажмите /help.\n\n";

    private static final String UNKNOWN_CLICK = "Неизвестная команда";

    /**
     * Формирует приветственное сообщение и предложение пройти тест.
     *
     * @param chatId
     * @return
     */
    public String startTest(long chatId) {
        return START_MESSAGE;
    }

    /**
     * Создает стартовую inline-клавиатуру с кнопками "Да" и "Нет".
     *
     * @param chatId
     * @return
     */
    public InlineKeyboardMarkup createStartButton(long chatId) {
        //уже создаем непосредственно кнопки
        // тоже нет конструктора надо делать как выше
        InlineKeyboardButton yes_button = InlineKeyboardButton.builder()
                .text("Конечно!")
                .callbackData("yes_button") // индификатор которы йпоказывает которая кнопка была нажата, она имеет тип стринг, позже пригодиться
                .build();

        InlineKeyboardButton no_button = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("no_button")
                .build();

        //список где кнопки хранятся грубо говоря в строку
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        //добавляем уже кнопки ряд
        rowInline.add(yes_button); //эта будет слева
        rowInline.add(no_button); //эта будет справа

        //список списков то же самое что и в строку только еще на столбцы разделили
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(rowInline);

        // создаём разметку клавиатуры и добавляем в сообщение
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;

    }

    /**
     * Обрабатывает нажатия на кнопки стартовой клавиатуры.
     *
     * @param callbackData
     * @param chatId
     * @return
     */
    public String handleButtonClick(String callbackData, long chatId) {
        switch (callbackData) {
            case "yes_button": {
                //ВНИАМНИЕ: тут класс создания и генерирования ответов

                //создаем экземпляр класса StartYesButton
                StartYesButton testGeneration = new StartYesButton();
                // генерация теста и возвращение его
                String test = testGeneration.getGeneratedTest();

                return testHandler.generateTest(chatId, test);

            }

            case "no_button":
                return NO_BUTTON_CLICK;
            default:
                return UNKNOWN_CLICK;
        }
    }

    /**
     * createAnswerKeyboard - метод, который создает кнопки для ответов на вопросы
     * @return
     */
}
