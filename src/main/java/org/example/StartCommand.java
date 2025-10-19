package org.example;


/**StartBot.java - класс который обрабатывает команнду /start,
 * а именно: высылает создает приветсвенное письмо и кнопки под ним,
 * ну и соответсвенно реакции на эти кнопки
 */

public class StartCommand {
    private final TestHandler testHandler;

    public StartCommand(TestHandler testHandler) {
        this.testHandler = testHandler;
    }

    private static final String START_MESSAGE = "Вас приветствует телеграмм бот GlobeTalk для изучения иностранных языков!\n\n" +
            "Перед началом обучения, пройдите короткий тестик для определения вашего уровня владения языка.\n\n" +
            "Для списка команд нажмите /help.\n\n"+
            "Вы готовы начать тест?\n\n";

    private static final String NO_BUTTON_CLICK = "Не сомневайтесь в себе!!!\n\n"+
            "Когда будете готовы используйте /start.\n\n"+
            "Для списка команд нажмите /help.\n\n";

    private static final String UNKNOWN_CLICK = "Неизвестная команда";

    /**
     * startTest - метод привествия, те после нажания команды /start сдоровается и высылает кнопками варианты ответов
     */
    public String startTest(){
        return START_MESSAGE;
    }

    /**
     * HandleButtonClick - метод обрабатывающий реакции, взятые с кнопок
     */
    public String handleButtonClick(String callbackData, long chatId){
        switch (callbackData){
            case "yes_button": {
                //ВНИАМНИЕ: тут класс создания и генерирования ответов

                //создаем экземпляр класса StartYesButton
                StartYesButton testGeneration = new StartYesButton();
                // генерация теста и возвращение его
                String test = testGeneration.generateTest();

                return testHandler.generateTest(chatId, test);

            }

            case "no_button":
                return NO_BUTTON_CLICK;
            default:
                return UNKNOWN_CLICK;
        }
    }
}
