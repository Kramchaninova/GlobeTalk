package org.example;

import org.example.Data.BotResponse;
import org.example.Data.KeyboardService;
import org.example.Data.UserService;
import org.example.SpeedTest.SpeedTestCommand;
import org.example.SpeedTest.SpeedTestHandler;
import org.example.StartTest.StartCommand;
import org.example.StartTest.TestHandler;
import org.example.Dictionary.DictionaryCommand;
import org.example.Dictionary.DictionaryServiceImpl;
import org.example.Authentication.AuthCommand;
import org.example.Authentication.AuthService;
import org.example.Authentication.AuthServiceImpl;
import org.example.ScheduledMessages.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    private final UserService userService;
    private final Message message;

    // Состояния пользователей для отслеживания активных процессов
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();

    public BotLogic() {
        this.userService = new UserService();
        this.testHandler = new TestHandler();
        this.speedTestHandler = new SpeedTestHandler();
        this.startCommand = new StartCommand(this.testHandler);
        this.speedTestCommand = new SpeedTestCommand(this.speedTestHandler);
        this.keyboardService = new KeyboardService();

        this.dictionaryCommand = new DictionaryCommand(new DictionaryServiceImpl());
        this.authService = new AuthServiceImpl();
        this.authCommand = new AuthCommand(authService);
        this.message = new Message();

        // Запускаем очистку неактивных пользователей
        startUserCleanupScheduler();
    }

    /**
     * Запускает таймер для очистки неактивных пользователей
     */
    private void startUserCleanupScheduler() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                userService.cleanupInactiveUsers();
            }
        }, TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1)); // Раз в день
    }

    public static final String COMMAND_HELP = "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

            "📋 **Доступные команды:**\n" +
            "• /start - Начать работу с ботом\n" +
            "• /start_test - Пройти тест на уровень языка\n" +
            "• /help - Показать эту справку\n" +
            "• /dictionary - Работа со словарем\n" +
            "• /speed_test - Пройти тест на скорость\n" +
            "• /word - Получить новое английское слово\n\n" +

            "🎯 **Как работает бот:**\n" +
            "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
            "• 📝 Тестирование для определения вашего уровня\n" +
            "• 🎮 Интерактивные упражнения\n" +
            "• 📚 Личный словарь\n" +
            "• 🔄 Ежедневные слова и повторения\n\n" +

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
        boolean telegramAuth = authService.isTelegramUserAuthorized(chatId);
        boolean discordAuth = authService.isDiscordUserAuthorized(chatId);
        boolean isAuthorized = telegramAuth || discordAuth;

        System.out.println("[Bot Logic] Проверка авторизации chatId " + chatId +
                ": Telegram=" + telegramAuth + ", Discord=" + discordAuth);

        if (isAuthorized) {
            userService.addToDistribution(chatId);
        } else {
            userService.removeFromDistribution(chatId);
        }

        return isAuthorized;
    }

    /**
     * Проверяет, занят ли пользователь другим процессом
     */
    public boolean isUserBusy(long chatId) {
        String state = userStates.get(chatId);
        boolean isBusy = state != null && (
                state.startsWith("waiting_") ||
                        state.startsWith("test_") ||
                        state.startsWith("speed_test_") ||
                        state.startsWith("dictionary_") ||
                        state.startsWith("profile_") ||
                        testHandler.isTestActive(chatId) ||
                        speedTestHandler.isTestActive(chatId)
        );

        if (isBusy) {
            System.out.println("[Bot Logic] Пользователь chatId " + chatId + " занят: " + state);
        }

        return isBusy;
    }

    /**
     * Проверяет, можно ли отправлять отложенные сообщения пользователю
     */
    public boolean canReceiveScheduledMessages(long chatId) {
        boolean isAuthorized = isUserAuthorized(chatId);
        boolean isBusy = isUserBusy(chatId);
        boolean canReceive = isAuthorized && !isBusy;

        System.out.println("[Bot Logic] Проверка рассылки для " + chatId +
                ": auth=" + isAuthorized + ", busy=" + isBusy + ", canReceive=" + canReceive);

        return canReceive;
    }

    /**
     * Устанавливает состояние пользователя
     */
    private void setUserState(long chatId, String state) {
        if (state == null) {
            userStates.remove(chatId);
            System.out.println("[Bot Logic] Очищено состояние для chatId " + chatId);
            // Если пользователь свободен, добавляем в рассылку если авторизован
            if (isUserAuthorized(chatId)) {
                userService.addToDistribution(chatId);
            }
        } else {
            userStates.put(chatId, state);
            System.out.println("[Bot Logic] Установлено состояние '" + state + "' для chatId " + chatId);
            // Если пользователь занят, убираем из рассылки
            userService.removeFromDistribution(chatId);
        }
    }

    /**
     * Генерирует отложенное сообщение с кнопками
     * Вызывается из TelegramBot/DiscordBot по таймеру
     */
    public BotResponse generateScheduledMessage(long chatId) {
        // Проверяем, можно ли отправлять сообщение
        if (!canReceiveScheduledMessages(chatId)) {
            System.out.println("[Bot Logic] Пользователь " + chatId + " занят, пропускаем отложенное сообщение");
            return null;
        }

        try {
            // Получаем уникальное слово для пользователя
            String wordMessage = message.getUniqueWordForUser(chatId);

            if (wordMessage == null || wordMessage.isEmpty()) {
                System.err.println("[Bot Logic] Не удалось получить слово для отложенного сообщения");
                return null;
            }

            System.out.println("[Bot Logic] Сгенерировано отложенное сообщение для пользователя " + chatId);

            return new BotResponse(chatId, wordMessage, "schedule_message");

        } catch (Exception e) {
            System.err.println("[Bot Logic] Ошибка генерации отложенного сообщения: " + e.getMessage());
            return null;
        }
    }

    /**
     * Получает активных пользователей для рассылки
     */
    public List<Long> getActiveUsersForDistribution() {
        Set<Long> activeUsers = userService.getActiveUsers();
        return new ArrayList<>(activeUsers);
    }

    /**
     * Очищает неактивных пользователей
     */
    public void cleanupInactiveUsers() {
        userService.cleanupInactiveUsers();
    }

    /**
     * Обработка ответов с кнопок
     */
    public String processCallbackData(String callbackData, long chatId) {
        System.out.println("[Bot Logic] Обработка callback: " + callbackData + " для chatId " + chatId);

        // Обновляем активность пользователя
        userService.updateUserActivity(chatId);

        // Обработка кнопок отложенных сообщений
        if (callbackData.equals("know_button") || callbackData.equals("learn_button")
                || callbackData.equals("more_word_button")){
            if (!isUserAuthorized(chatId)) {
                return NOT_AUTHORIZED_MESSAGE;
            }
            if (isUserBusy(chatId)) {
                return "⏳ Сначала завершите текущее действие";
            }

            // Message сам генерирует новое слово, добавляет в БД и возвращает следующее слово
            return message.handleWordButtonClick(callbackData, chatId);
        }

        if (callbackData.equals("next_scheduled_word")) {
            if (!isUserAuthorized(chatId)) {
                return NOT_AUTHORIZED_MESSAGE;
            }
            if (isUserBusy(chatId)) {
                return "⏳ Сначала завершите текущее действие";
            }
            // Генерируем следующее слово
            return message.getUniqueWordForUser(chatId);
        }

        // Кнопки аутентификации доступны без авторизации
        if (callbackData.equals("main_button")) {
            setUserState(chatId, null);
            return COMMAND_HELP;
        }
        else if (callbackData.equals("sing_in_button") ||
                callbackData.equals("reg_button") ||
                callbackData.equals("login_again_button") ||
                callbackData.equals("start_button") ||
                callbackData.equals("log_out_cancel_button")) {
            return authCommand.handleButtonClick(callbackData, chatId, true);
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
                setUserState(chatId, "test_active");
                String result = testHandler.handleAnswer(callbackData, chatId);
                if (result.contains("Тест завершён")) {
                    setUserState(chatId, null);
                }
                return result;
            } else if (speedTestHandler.isTestActive(chatId)) {
                setUserState(chatId, "speed_test_active");
                var result = speedTestHandler.handleAnswerWithFeedback(callbackData, chatId);
                if (((String) result.get("feedback")).contains("Тест завершён")) {
                    setUserState(chatId, null);
                }
                return (String) result.get("feedback");
            } else {
                return "Сначала начните тест командой /start_test или /speed_test";
            }
        } else if (callbackData.equals("speed_yes_button") ||
                callbackData.equals("speed_no_button")) {
            setUserState(chatId, "speed_test_start");
            return speedTestCommand.handleButtonClick(callbackData, chatId);
        } else if (callbackData.equals("next_button")) {
            if (speedTestHandler.isTestActive(chatId)) {
                return speedTestHandler.moveToNextQuestion(chatId);
            } else {
                setUserState(chatId, null);
                return "Тест не активен";
            }
        } else if (callbackData.startsWith("dictionary_")) {
            setUserState(chatId, "dictionary_active");

            // Обрабатываем специфичные состояния словаря
            if (callbackData.equals("dictionary_add_button")) {
                setUserState(chatId, "waiting_add_word");
            } else if (callbackData.equals("dictionary_edit_button")) {
                setUserState(chatId, "waiting_edit_word");
            } else if (callbackData.equals("dictionary_delete_button")) {
                setUserState(chatId, "waiting_delete_word");
            } else if (callbackData.equals("dictionary_add_no_button") ||
                    callbackData.equals("dictionary_delete_cancel_button")) {
                setUserState(chatId, "dictionary_active");
            }

            return dictionaryCommand.handleButtonClick(callbackData, chatId);
        }
        // Кнопки профиля (требуют авторизации)
        else if (callbackData.equals("login_edit_button") ||
                callbackData.equals("password_edit_button") ||
                callbackData.equals("log_out_button") ||
                callbackData.equals("log_out_final_button")||
                callbackData.equals("my_profile_button")) {
            setUserState(chatId, "profile_active");
            return authCommand.handleButtonClick(callbackData, chatId, true);
        }
        // Обработка остальных кнопок
        else {
            setUserState(chatId, "test_start");
            return startCommand.handleButtonClick(callbackData, chatId);
        }
    }

    /**
     * Обработка команд
     */
    public BotResponse handleCommand(String command, long chatId) {
        System.out.println("[Bot Logic] Обработка команды: " + command + " для chatId " + chatId);

        // Регистрируем пользователя при любой команде
        userService.addUser(chatId);
        userService.updateUserActivity(chatId);

        String responseText;
        String keyboardType = null;

        switch (command) {
            case "/start":
                setUserState(chatId, null);
                responseText = authCommand.getStartMessage(chatId);
                keyboardType = !isUserAuthorized(chatId) ? "sing_in_main" : null;
                break;
            case "/my_profile":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    setUserState(chatId, "profile_view");
                    responseText = authCommand.getUserProfileMessage(chatId);
                    keyboardType = "my_profile";
                }
                break;
            case "/start_test":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    setUserState(chatId, "test_start");
                    responseText = startCommand.startTest();
                    keyboardType = "start";
                }
                break;
            case "/speed_test":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    setUserState(chatId, "speed_test_start");
                    responseText = speedTestCommand.startTest();
                    keyboardType = "speed_test_start";
                }
                break;
            case "/dictionary":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    setUserState(chatId, "dictionary_view");
                    responseText = dictionaryCommand.showDictionary(chatId);
                    keyboardType = "dictionary";
                }
                break;
            case "/word":
                if (!isUserAuthorized(chatId)) {
                    responseText = NOT_AUTHORIZED_MESSAGE;
                    keyboardType = "sing_in_main";
                } else {
                    setUserState(chatId, "word_request");
                    responseText = message.getUniqueWordForUser(chatId);
                    setUserState(chatId, null); // Сразу освобождаем состояние
                    keyboardType = "schedule_message";
                }
                break;
            case "/help":
                setUserState(chatId, null);
                responseText = COMMAND_HELP;
                break;
            default:
                responseText = COMMAND_UNKNOWN;
        }

        System.out.println("[Bot Logic] Ответ на команду '" + command);

        return new BotResponse(chatId, responseText, keyboardType);
    }

    /**
     * Обрабатывает callback запросы от кнопок.
     */
    public BotResponse processCallback(String callbackData, long chatId) {
        // Обновляем активность пользователя
        userService.updateUserActivity(chatId);

        String responseText = processCallbackData(callbackData, chatId);
        String keyboardType = getKeyboardForCallback(callbackData, chatId);

        System.out.println("[Bot Logic] Callback обработан, ответ: " + responseText);

        return new BotResponse(chatId, responseText, keyboardType);
    }

    /**
     * Обрабатывает текстовые сообщения от пользователя.
     */
    public BotResponse processMessage(String messageText, long chatId) {
        System.out.println("[Bot Logic] Получено сообщение от chatId " + chatId + ": " + messageText);

        // Регистрируем пользователя при первом сообщении
        userService.addUser(chatId);
        userService.updateUserActivity(chatId);

        if (messageText.startsWith("/")) {
            return handleCommand(messageText, chatId);
        } else {
            // Обработка текстовых сообщений для аутентификации
            String authResponse = authCommand.handleTextMessage(messageText, chatId, true);
            if (!authResponse.equals(authCommand.getStartMessage())) {
                System.out.println("[Bot Logic] Обработка аутентификации для chatId " + chatId);
                String keyboardType = determineAuthKeyboardType(authResponse);
                return new BotResponse(chatId, authResponse, keyboardType);
            }

            // Обработка текстовых команд для словаря (только для авторизованных)
            if (isUserAuthorized(chatId)) {
                String responseText = dictionaryCommand.handleTextCommand(messageText, chatId);
                if (responseText != null && !responseText.isEmpty()) {
                    System.out.println("[Bot Logic] Обработка команды словаря для chatId " + chatId);

                    // Обновляем состояние на основе ответа словаря
                    if (responseText.contains("Новое слово добавлено!") ||
                            responseText.contains("Удаление отменено") ||
                            responseText.contains("Перевод успешно обновлён")) {
                        setUserState(chatId, "dictionary_active");
                    } else if (responseText.contains("Пополнить еще словарь?")) {
                        setUserState(chatId, "waiting_add_decision");
                    } else {
                        setUserState(chatId, null);
                    }

                    String keyboardType = determineKeyboardType(responseText);
                    return new BotResponse(chatId, responseText, keyboardType);
                }
            }

            System.out.println("[Bot Logic] Неизвестная команда от chatId " + chatId);
            return new BotResponse(chatId, "Не понимаю команду. Введите /help для справки.");
        }
    }

    /**
     * Метод для определения типа клавиатуры на основе текста ответа аутентификации
     */
    private String determineAuthKeyboardType(String responseText) {
        if (responseText.contains("Регистрация в GlobeTalk")) {
            return "";
        } else if (responseText.contains("Вход в аккаунт GlobeTalk")) {
            return "";
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
     * Метод для определение типа клавиатуры на основе текста ответа
     */
    private String determineKeyboardType(String responseText) {
        if (responseText.contains("✨ *Добро пожаловать в ваш личный словарь!* ✨")) {
            return "dictionary";
        }
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
        else if (responseText.contains("🎉 **Новое слово!** 🎉")) {
            return "schedule_message";
        } else if (responseText.contains("Слово уже добавлено в словарь для изучения!")){
            return "schedule_message_final";

        }
        return "";
    }

    /**
     * Метод определения ключа показываемого списка кнопок после нажатия
     */
    public String getKeyboardForCallback(String callbackData, long chatId) {
        System.out.println("[Bot Logic] Определение клавиатуры для callback: " + callbackData);

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
            case "know_button"-> {
                return "schedule_message";
            }
            case "learn_button" ->{
                return "schedule_message_final";
            }
            case "more_word_button" ->{
                return "schedule_message";
            }
        }
        return null;
    }

    /**
     * Логика определения типа команды в боковом меню
     */
    public String getKeyboardForCommand(String command) {
        System.out.println("[Bot Logic] Определение клавиатуры для команды: " + command);

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
                case "/word":
                    return "schedule_message";
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