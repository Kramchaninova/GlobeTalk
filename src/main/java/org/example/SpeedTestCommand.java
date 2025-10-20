package org.example;

/**
 * SpeedTestCommand - управление тестом на скорость
 * Наследует логику обычного теста через StartCommand
 */
public class SpeedTestCommand extends StartCommand {

    public SpeedTestCommand(TestHandler testHandler) {
        super(testHandler); // наследуем TestHandler
    }

    private static final String START_MESSAGE = "Вы выбрали тест на скорость!\n" +
            "Хотите начать тест прямо сейчас?\n\n";

    private static final String NO_BUTTON_CLICK = "Хорошо, вы можете пройти тест позже.\n\n";

    /**
     * startTest - возвращает приветственное сообщение и кнопки Да/Нет
     */
    @Override
    public String startTest() {
        return START_MESSAGE;
    }

    /**
     * handleButtonClick - обрабатывает нажатия кнопок Да/Нет
     */
    @Override
    public String handleButtonClick(String callbackData, long chatId) {
        switch (callbackData) {
            case "speed_yes_button": {
                // Создаём тест через SpeedTestYesButton
                SpeedTestYesButton generator = new SpeedTestYesButton();
                String test = generator.generateTest();
                // Используем getTestHandler() для генерации теста и получения первого вопроса
                return super.getTestHandler().generateTest(chatId, test);
            }
            case "speed_no_button":
                return NO_BUTTON_CLICK;
            default:
                return "Неизвестная команда";
        }
    }
}
