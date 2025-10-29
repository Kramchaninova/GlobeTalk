package org.example;

/**
 * SpeedTestCommand - управление тестом на скорость
 * Наследует логику обычного теста через StartCommand
 */
public class SpeedTestCommand{
    private final SpeedTestHandler speedTestHandler;

    public SpeedTestCommand(SpeedTestHandler speedTestHandler) {
        this.speedTestHandler = speedTestHandler;
    }

    private static final String START_MESSAGE = "🌍 *Добро пожаловать в тест на скорость!* 🌍\n\n" +
            "⚡ **Тест на скорость реакции** ⚡\n\n" +
            "Проверьте, насколько быстро вы можете отвечать на вопросы!\n\n" +
            "📊 **Как это работает:**\n" +
            "• Вам будут показаны вопросы с вариантами ответов\n" +
            "• Отвечайте как можно быстрее\n" +
            "• В конце получите статистику ответов\n\n" +
            "🎯 **Особенности теста:**\n" +
            "• Таймер отслеживает скорость ваших ответов\n" +
            "• Можно перейти к следующему вопросу кнопкой \"Дальше\"\n" +
            "• Результаты помогут оценить вашу реакцию\n\n" +
            "🚀 **Начнем тест на скорость?**";

    private static final String NO_BUTTON_CLICK = "😊 **Отлично, решение принято!**\n\n" +
            "📅 Вы можете пройти тест на скорость в любое удобное время\n\n" +
            "💡 **Когда будете готовы:**\n" +
            "• Используйте команду /speed_test\n" +
            "• Или выберите тест в меню бота\n\n" +
            "🌟 **А пока можете:**\n" +
            "• Пройти основной тест командой /start\n" +
            "• Изучить возможности бота через /help\n\n" +
            "🕐 **Вернемся к тесту, когда вам будет удобно!**";

    /**
     * startTest - возвращает приветственное сообщение и кнопки Да/Нет
     */
    public String startTest() {
        return START_MESSAGE;
    }

    /**
     * handleButtonClick - обрабатывает нажатия кнопок Да/Нет
     */
    public String handleButtonClick(String callbackData, long chatId) {
        switch (callbackData) {
            case "speed_yes_button": {
                SpeedTestYesButton generator = new SpeedTestYesButton();
                String test = generator.generateTest();
                return speedTestHandler.generateTest(chatId, test);
            }
            case "speed_no_button":
                return NO_BUTTON_CLICK;
            default:
                return "Неизвестная команда";
        }
    }
}
