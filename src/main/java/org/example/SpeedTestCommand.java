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

    private static final String START_MESSAGE = "Вы выбрали тест на скорость!\n" +
            "Хотите начать тест прямо сейчас?\n\n";

    private static final String NO_BUTTON_CLICK = "Хорошо, вы можете пройти тест позже.\n\n";

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
