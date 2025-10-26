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

    private static final String START_MESSAGE = "🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
            "Ваш персональный помощник в изучении иностранных языков! 🎯\n\n" +
            "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n" +
            "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
            "🔍 Для просмотра всех команд нажмите /help\n\n" +
            "🚀 **Вы готовы начать тест?**";

    private static final String NO_BUTTON_CLICK = "💪 *Не сомневайтесь в своих силах!* 💪\n\n" +
            "📖 Тест займет всего несколько минут и поможет определить ваш текущий уровень\n\n" +
            "🕐 Когда будете готовы - просто нажмите /start\n\n" +
            "🔍 Все команды доступны по /help";

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
