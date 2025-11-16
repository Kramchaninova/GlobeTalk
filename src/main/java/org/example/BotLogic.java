package org.example;

import org.example.Data.BotResponse;
import org.example.Data.KeyboardService;
import org.example.SpeedTest.SpeedTestCommand;
import org.example.SpeedTest.SpeedTestHandler;
import org.example.StartTest.StartCommand;
import org.example.StartTest.TestHandler;
import org.example.Dictionary.DictionaryCommand;
import org.example.Dictionary.DictionaryServiceImpl;
import org.example.Authentication.AuthCommand;
import org.example.Authentication.AuthService;
import org.example.Authentication.AuthServiceImpl;

/**
 * BotLogic - класс для обработки логики бота.
 * обрабатывает входящие сообщения, команды и callback запросы от кнопок
 */

public class BotLogic {
    private final StartCommand startCommand;
    private final SpeedTestCommand speedTestCommand;
    private final TestHandler testHandler;
    private final KeyboardService keyboardService;
    private final SpeedTestHandler speedTestHandler;
    private final DictionaryCommand dictionaryCommand;
    private final AuthCommand authCommand;
    private final AuthService authService;

    public BotLogic() {
        this.testHandler = new TestHandler();
        this.speedTestHandler = new SpeedTestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.speedTestCommand = new SpeedTestCommand(this.speedTestHandler);
        this.keyboardService = new KeyboardService();

        DictionaryServiceImpl dictionaryService = new DictionaryServiceImpl();
        this.dictionaryCommand = new DictionaryCommand(dictionaryService);

        this.authService = new AuthServiceImpl();
        this.authCommand = new AuthCommand(authService);
    }

    public static final String COMMAND_HELP = "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

            "📋 **Доступные команды:**\n" +
            "• /start- Начать работу с ботом\n" +
            "• /start_test - Пройти тест на уровень языка\n" +
            "• /help - Показать эту справку\n" +
            "• /dictionary - Работа со словарем\n" +
            "• /speed_test - Пройти тест на скорость\n\n" +

            "🎯 **Как работает бот:**\n" +
            "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
            "• 📝 Тестирование для определения вашего уровня\n" +
            "• 🎮 Интерактивные упражнения\n\n" +

            "🛠️ **В процессе разработки:**\n" +
            "• 📊 Отслеживание прогресса\n" +
            "• 📚 Словарь и словарный запас\n\n"+

            "💡 **Как взаимодействовать:**\n" +
            "• Используйте команды из меню (слева)\n" +
            "• Нажимайте на кнопки под сообщениями\n" +
            "• Отвечайте на вопросы теста\n" +
            "• Следите за своим прогрессом в профиле\n\n" +

            "🚀 **Начните с команды /start_test чтобы определить ваш уровень!**";

    private static final String COMMAND_UNKNOWN = "Неизвестная команда. Введите /help для списка доступных команд.";
    private static final String NOT_AUTHORIZED_MESSAGE = "❌ **Доступ запрещен!**\n\n" +
            "Для использования этой функции необходимо войти в аккаунт.\n\n" +
            "🔐 Используйте команду /start для регистрации или входа.";

    /**
     * Проверяет авторизацию пользователя
     */
    private boolean isUserAuthorized(long chatId) {
        // Проверяем и в Telegram и в Discord
        return authService.isTelegramUserAuthorized(chatId) ||
                authService.isDiscordUserAuthorized(chatId);
    }

    /**
     * Обработка ответов с кнопок
     */
    public String processCallbackData(String callbackData, long chatId) {
        // Кнопки аутентификации доступны без авторизации
        if (callbackData.equals("main_button")) {
            return COMMAND_HELP;
        }
        else if (callbackData.equals("sing_in_button") ||
                callbackData.equals("reg_button") ||
                callbackData.equals("login_again_button") ||
                callbackData.equals("start_button") ||
                callbackData.equals("log_out_cancel_button")) {
            return authCommand.handleButtonClick(callbackData, chatId, true); // isTelegram = true для Telegram
        }
        // Все остальные функции требуют авторизации
        else if (!isUserAuthorized(chatId)) {
            return NOT_AUTHORIZED_MESSAGE;
        }
        else if (callbackData.equals("A_button") ||
                callbackData.equals("B_button") ||
                callbackData.equals("C_button") ||
                callbackData.equals("D_button")) {
            if (testHandler.isTestActive(chatId)) {
                return testHandler.handleAnswer(callbackData, chatId);
            } else if (speedTestHandler.isTestActive(chatId)) {
                var result = speedTestHandler.handleAnswerWithFeedback(callbackData, chatId);
                return (String) result.get("feedback");
            } else {
                return "Сначала начните тест командой /start_test или /speed_test";
            }
        } else if (callbackData.equals("speed_yes_button") ||
                callbackData.equals("speed_no_button")) {
            return speedTestCommand.handleButtonClick(callbackData, chatId);
        } else if (callbackData.equals("next_button")) {
            if (speedTestHandler.isTestActive(chatId)) {
                return speedTestHandler.moveToNextQuestion(chatId);
            } else {
                return "Тест не активен";
            }
        } else if (callbackData.startsWith("dictionary_")) {
            return dictionaryCommand.handleButtonClick(callbackData, chatId);
        }
        // Кнопки профиля (требуют авторизации)
        else if (callbackData.equals("login_edit_button") ||
                callbackData.equals("password_edit_button") ||
                callbackData.equals("log_out_button") ||
                callbackData.equals("log_out_final_button")||
                callbackData.equals("my_profile_button")) {
            return authCommand.handleButtonClick(callbackData, chatId, true);
        }
        // Обработка остальных кнопок
        else {
            return startCommand.handleButtonClick(callbackData, chatId);
        }
    }

    /**
     * Если в сообщении была команда, т.е. текст начинается с /, то обрабатываем ее
     * и высылаем текст, который привязан к командам
     */
    BotResponse handleCommand(String command, long chatId) {
        String responseText;
        String keyboardType = null;

        switch (command) {
            case "/start":
                responseText = authCommand.getStartMessage(chatId);
                keyboardType = !isUserAuthorized(chatId) ? "sing_in_main" : null;
                break;
            case "/my_profile":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    responseText = authCommand.getUserProfileMessage(chatId);
                    keyboardType = "my_profile";
                }
                break;
            case "/start_test":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    responseText = startCommand.startTest();
                    keyboardType = "start";
                }
                break;
            case "/speed_test":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    responseText = speedTestCommand.startTest();
                    keyboardType = "speed_test_start";
                }
                break;
            case "/dictionary":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    responseText = dictionaryCommand.showDictionary(chatId);
                    keyboardType = "dictionary";
                }
                break;
            case "/help":
                responseText = COMMAND_HELP;
                break;
            default:
                responseText = COMMAND_UNKNOWN;
        }

        return new BotResponse(chatId, responseText, keyboardType);
    }

    /**
     * Обрабатывает callback запросы от кнопок.
     */
    public BotResponse processCallback(String callbackData, long chatId) {
        String responseText = processCallbackData(callbackData, chatId);
        String keyboardType = getKeyboardForCallback(callbackData, chatId);

        return new BotResponse(chatId, responseText, keyboardType);
    }

    /**
     * Обрабатывает текстовые сообщения от пользователя.
     */
    public BotResponse processMessage(String messageText, long chatId) {
        if (messageText.startsWith("/")) {
            System.out.println("Обработана команда из бокового меню: " + messageText);
            return handleCommand(messageText, chatId);
        } else {
            // Обработка текстовых сообщений для аутентификации
            String authResponse = authCommand.handleTextMessage(messageText, chatId, true);
            if (!authResponse.equals(authCommand.getStartMessage())) {
                // Если это ответ в процессе аутентификации
                String keyboardType = determineAuthKeyboardType(authResponse);
                return new BotResponse(chatId, authResponse, keyboardType);
            }

            // Обработка текстовых команд для словаря (только для авторизованных)
            if (isUserAuthorized(chatId)) {
                String responseText = dictionaryCommand.handleTextCommand(messageText, chatId);
                if (responseText != null && !responseText.isEmpty()) {
                    String keyboardType = determineKeyboardType(responseText);
                    return new BotResponse(chatId, responseText, keyboardType);
                }
            }

            // Если не команда словаря и не аутентификация, обрабатываем как обычное сообщение
            return new BotResponse(chatId, "Не понимаю команду. Введите /help для справки.");
        }
    }

    /**
     * Метод для определения типа клавиатуры на основе текста ответа аутентификации
     */
    private String determineAuthKeyboardType(String responseText) {
        if (responseText.contains("Регистрация в GlobeTalk")) {
            return ""; // Без клавиатуры для регистрации
        } else if (responseText.contains("Вход в аккаунт GlobeTalk")) {
            return ""; // Без клавиатуры для входа
        } else if (responseText.contains("Регистрация завершена")){
            return "sing_in_end";
        } else if (responseText.contains("Кажется, у нас проблемка")) {
            return "login_error";
        } else if (responseText.contains("**Логин изменен!**") || responseText.contains("**Пароль изменен!**")) {
            return "login_password_edit_end";
        }
        return "";
    }

    /**
     * Метод для определения типа клавиатуры на основе текста ответа
     */
    private String determineKeyboardType(String responseText) {
        if (responseText.contains("✨ *Добро пожаловать в ваш личный словарь!* ✨")) {
            return "dictionary";
        }
        // Если это успешное добавление слова - показываем кнопку add_again
        else if (responseText.contains("Новое слово добавлено!") ||
                responseText.contains("Пополнить еще словарь?")) {
            return "add_again";
        }
        else if (responseText.contains("*Подтвердите удаление*")) {
            return "delete";
        }
        else if (responseText.contains("Отлично! Перевод успешно обновлён ✅")) {
            return "dictionary_final_button";
        }
        return "";
    }

    /**
     * Метод определения ключа показываемого списка кнопок после нажатия
     */
    public String getKeyboardForCallback(String callbackData, long chatId) {
        switch (callbackData) {
            case "yes_button" -> {
                return "test_answers";
            }
            case "A_button", "B_button", "C_button", "D_button" -> {
                if (testHandler.isTestActive(chatId)) {
                    return "test_answers";
                } else if (speedTestHandler.isTestActive(chatId)) {
                    return "speed_test_next";
                } else {
                    // Если тест завершен - показываем кнопку на главную
                    return "main";
                }
            }
            case "no_button" -> {return "main";}
            case "speed_yes_button" -> {
                return "test_answers";
            }
            case "next_button" -> {
                if (speedTestHandler.isTestActive(chatId)) {
                    return "test_answers";
                } else {
                    // Если тест завершен - показываем кнопку на главную
                    return "main";
                }
            }
            case "dictionary_button"-> {
                return "dictionary";
            }
            case "dictionary_add_no_button" -> {
                return "dictionary";
            }
            case "dictionary_delete_cancel_button" -> {
                return "delete_cancel";
            }
            case "dictionary_delete_confirm_button" -> {
                return "dictionary_final_button";
            }
            case "start_button" -> {
                return "sing_in_main";
            }
            case "log_out_button" -> {
                return "log_out_confirm";
            }
            case "log_out_final_button" -> {
                return "sing_in_main";
            }
            case "my_profile_button" ->{
                return "my_profile";
            }
        }
        return null;
    }

    /** логика определения типа команды в боковом меню
     *
     * @param command
     * @return
     */
    public String getKeyboardForCommand(String command) {
        if (command != null) {
            switch (command) {
                case "/start":
                    return "sing_in_main";
                case "/start_test":
                    return "start";
                case "/speed_test":
                    return "speed_test_start";
                case "/dictionary":
                    return "dictionary";
                case "/my_profile":
                    return "my_profile";
                default:
                    return null;
            }
        }
        return null;
    }

    public KeyboardService getKeyboardService() {
        return keyboardService;
    }
}