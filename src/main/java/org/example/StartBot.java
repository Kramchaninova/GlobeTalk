package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**StartBot.java - класс который обрабатывает команнду /start,
 * а именно: высылает создает приветсвенное письмо и кнопки под ним,
 * ну и соответсвенно реакции на эти кнопки
 */

public class StartBot {

    /**
     * startTest - метод привествия, те после нажания команды /start сдоровается и высылает кнопками варианты ответов
     * @param chatId
     * @return
     */
    public SendMessage startTest(long chatId) {

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Вас приветствует телеграмм бот GlobeTalk для изучения иностранных языков!\n\n" +
                        "Перед началом обучения, пройдите короткий тестик для определения вашего уровня владения языка.\n\n" +
                        "Для списка команд нажмите /help.\n\n"+
                        "Вы готовы начать тест?\n\n")
                .build();

        //уже создаем непосредственно кнопки
        // тоже нет конструктора надо делать как выше
        InlineKeyboardButton yes_button = InlineKeyboardButton.builder()
                .text("Конечно!")
                .callbackData("yes_button") // индификатор которы йпоказывает которая кнопка была нажата, она имеет тип стринг, позже пригодиться
                .build();

        InlineKeyboardButton no_button = InlineKeyboardButton.builder()
                .text("Совневаюсь...")
                .callbackData("no_button")
                .build();

        //список где кнопки хранятся грубо говоря в строку
        List<InlineKeyboardButton> rowInline  = new ArrayList<>();

        //добавляем уже кнопки ряд
        rowInline.add(yes_button); //эта будет слева
        rowInline.add(no_button); //эта будет справа

        //список списков то же самое что и в строку только еще на столбцы разделили
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(rowInline);

        // создаём разметку клавиатуры и добавляем в сообщение
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        return message;

    }

    /**
     * HandleButtonClick - метод обрабатывающий реакции, взятые с кнопок
     * @param callbackData
     * @param chatId
     * @return
     */

    public SendMessage HandleButtonClick(String callbackData, long chatId){
        switch (callbackData){
            case "yes_button":
                //ВНИАМНИЕ: тут класс создания и генерирования ответов

                //создаем экземпляр класса StartYesButton
                StartYesButton testGeneration = new StartYesButton();
                // генерация теста и возвращение его
                String test = testGeneration.getGeneratedTest();

                System.out.println("Сгенерированный тест:");
                System.out.println(test);

                // временное возвращение (часть ники), тут потом будет метод который будет высылать сообщения потоком
                // вместо с кнопками
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("ПОГНАЛИИИ!!!\n\n")
                        .build();


            case "no_button":
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Не сомневайтесь в себе!!!\n\n"+
                                "Когда будете готовы используйте /start.\n\n"+
                                "Для списка команд нажмите /help.\n\n")
                        .build();
            default:
                return SendMessage.builder()
                        .chatId(chatId)
                        .text("Неизвестная команда")
                        .build();
        }
    }
}
