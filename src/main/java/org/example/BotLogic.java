package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.ArrayList;
import java.util.List;

/**
 * BotLogic - класс для обработки логики бота.
 * обрабатывает входящие сообщения, команды и callback запросы от кнопок
 */


public class BotLogic {
    private final StartCommand startCommand;
    private final TestHandler testHandler;
    private final KeyboardService keyboardService;

    public BotLogic(){
        this.testHandler = new TestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.keyboardService = new KeyboardService();
    }

    private static final String COMMAND_HELP =  "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

            "📋 **Доступные команды:**\n\n" +
            "• /start - Начать работу с ботом и пройти тестирование\n" +
            "• /help - Показать эту справку\n" +

            "🎯 **Как работает бот:**\n\n" +
            "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
            "• 📝 Тестирование для определения вашего уровня\n\n" +

            "🛠️ **В процессе разработки:****\n" +
            "• 🎮 Интерактивные упражнения\n" +
            "• 📊 Отслеживание прогресса\n\n" +

            "💡 **Как взаимодействовать:**\n" +
            "• Используйте команды из меню (слева)\n" +
            "• Нажимайте на кнопки под сообщениями\n" +
            "• Отвечайте на вопросы теста\n" +
            "• Следите за своим прогрессом в профиле\n\n" +

            "🚀 **Начните с команды /start чтобы определить ваш уровень!**";


    private static final String COMMAND_UNKNOWN = "Неизвестная команда. Введите /help для списка доступных команд.";

    /**
     * Обработка ответов с кнопок
     */
    public String processCallbackData(String callbackData, long chatId) {
        if (callbackData.equals("A_button") ||
                callbackData.equals("B_button") ||
                callbackData.equals("C_button") ||
                callbackData.equals("D_button")) {
            return testHandler.handleAnswer(callbackData, chatId);
        } else {
            return startCommand.handleButtonClick(callbackData, chatId);
        }
    }

    /**
     * Если в сообщении была команда, т.е. текст начинается с /, то обрабатываем ее
     *и высылаем текст, который привязан к командам
     */
    public String handleCommand(String command) {
        switch (command) {
            case "/start":
                // StartCommand - отельный класс для реализации старта бота,в дальнейшем логично было бы на каждую задачу выводить по классу
                return startCommand.startTest();

            case "/help":
                return COMMAND_HELP;

            default:
                return COMMAND_UNKNOWN;
        }

    }

    /**
     * handleCallbackQuery - собирает результаты обработки в список
     */
    public List<String> handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        String responseText = processCallbackData(callbackData, chatId);
        String keyboardType = getKeyboardForCallback(callbackData, chatId);

        // возвращаем список: [chatId, responseText, keyboardType]
        List<String> result = new ArrayList<>();
        result.add(String.valueOf(chatId));
        result.add(responseText);
        result.add(keyboardType != null ? keyboardType : "");

        return result;
    }


    // обработка всех входящих сообщений
    public List<String> onUpdateReceived(Update update) {
        List<String> result = new ArrayList<>();

        // обработка нажатий кнопок
        if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update);

        //обработка сообщения или команды ( в нашем случае команды веденную с клавиатуры))
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // команда из бокового меню
            if (messageText.startsWith("/")) {
                String responseText = handleCommand(messageText);
                String keyboardType = getKeyboardForCommand(messageText, chatId);

                result.add(String.valueOf(chatId));
                result.add(responseText);
                result.add(keyboardType != null ? keyboardType : "");

                System.out.println("обработана команда из бокового меню: " + messageText);
            }
        }
        return result;
    }


    /**
     *  метод определения ключа показываемого списка кнопок после нажатия
     */
    public String getKeyboardForCallback(String callbackData, long chatId) {
        switch (callbackData) {
            case "yes_button" -> { return "test_answers"; }
            case "A_button", "B_button", "C_button", "D_button" -> {
                if (testHandler.isTestActive(chatId)) { return "test_answers"; }
            }
            case "no_button" -> { return "start"; }
        }
        return null;
    }

    //логика определения типа команды в боковом меню
    public String getKeyboardForCommand(String command, long chatId) {
        if (command != null && command.equals("/start")) {
            return "start";
        }
        return null;
    }
    public KeyboardService getKeyboardService() {
        return keyboardService;
    }
}
