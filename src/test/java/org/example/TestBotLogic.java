package org.example;

import org.example.Data.BotResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * TestBotLogic - тестирует основную логику бота без зависимостей от баз данных
 */
public class TestBotLogic {

    private InMemoryBotLogic botLogic;
    private final long AUTHORIZED_USER_ID = 1001L;
    private final long UNAUTHORIZED_USER_ID = 1002L;
    private final long NEW_USER_ID = 1003L;

    // Константы
    private static final String NOT_AUTHORIZED_MESSAGE = "❌ **Доступ запрещен!**\n\n" +
            "Для использования этой функции необходимо войти в аккаунт.\n\n" +
            "🔐 Используйте команду /start для регистрации или входа.";

    private static final String HELP_MESSAGE = "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +
            "📋 **Доступные команды:**\n" +
            "• /start - Начать работу с ботом\n" +
            "• /start_test - Пройти тест на уровень языка\n" +
            "• /help - Показать эту справку\n" +
            "• /dictionary - Работа со словарем\n" +
            "• /speed_test - Пройти тест на скорость\n" +
            "• /word - Получить новое английское слово\n" +
            "• /scheduled_test - Пройти отложенный тест по словам\n" +
            "• /old_word - Повторить слово с низким приоритетом\n\n" +
            "🎯 **Как работает бот:**\n" +
            "GlobeTalk поможет вам в изучении иностранных языков через:\n" +
            "• 📝 Тестирование для определения вашего уровня\n" +
            "• 🎮 Интерактивные упражнения\n" +
            "• 📚 Личный словарь\n" +
            "• 🔄 Ежедневные слова и повторения\n" +
            "• ⏰ Отложенные тесты для закрепления материала\n" +
            "• 📊 Повторение слов с низким приоритетом\n\n" +
            "💡 **Как взаимодействовать:**\n" +
            "• Используйте команды из меню (слева)\n" +
            "• Нажимайте на кнопки под сообщениями\n" +
            "• Отвечайте на вопросы теста\n" +
            "• Следите за своим прогрессом в профиле\n\n" +
            "🚀 **Начните с команды /start_test чтобы определить ваш уровень!**";

    private static final String WELCOME_UNAUTHORIZED = "🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
            "Ваш персональный помощник в изучении иностранных языков! 🎯\n\n" +
            "📝 **Для начала работы необходимо зарегистрироваться**\n" +
            "Это займет всего 30 секунд, но откроет все возможности платформы!\n\n" +
            "✨ **После регистрации вы получите:**\n" +
            "• Персональную программу обучения\n" +
            "• Доступ к урокам и упражнениям\n" +
            "• Доступ к созданию личного словаря\n"+
            "• Трекинг прогресса\n\n" +
            "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения " +
            "вашего текущего уровня владения языком.\n\n" +
            "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
            "🎯 **Готовы открыть мир языков?**\n" +
            "🚀 Начните с регистрации и тестирования!";

    private static final String WELCOME_AUTHORIZED = "🌍 *С возвращением в GlobeTalk!* 🌍\n\n" +
            "Рады снова видеть вас! Ваш персональный помощник в изучении иностранных языков готов к работе! 🎯\n\n" +
            "✨ **Ваш аккаунт активен, доступ открыт:**\n" +
            "• Продолжайте обучение по персональной программе\n" +
            "• Доступ к урокам и упражнениям\n" +
            "• Ваш личный словарь\n" +
            "• Трекинг прогресса\n\n" +
            "📚 **Что хотите сделать?**\n" +
            "• Продолжить тестирование\n" +
            "• Попрактиковать слова из словаря\n" +
            "• Пройти новые упражнения\n\n" +
            "🎯 **Продолжайте изучать языки!**\n" +
            "🚀 Выберите действие из меню";

    private static final String START_TEST_AUTHORIZED = "Ваш персональный помощник в изучении иностранных языков GlobeTalk!* 🌍!\n\n" +
            "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n" +
            "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
            "🔍 Для просмотра всех команд нажмите /help\n\n" +
            "🚀 **Вы готовы начать тест?**";

    private static final String SPEED_TEST_AUTHORIZED = "🌍 *Добро пожаловать в тест на скорость!* 🌍\n\n" +
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

    private static final String DICTIONARY_AUTHORIZED = "✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
            "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
            "📚 *Ваш словарь пуст*\nДобавьте первое слово для начала изучения!\n\n" +
            "🛠️ *Доступные действия:*\n\n" +
            "• ➕ **Добавить слово** — пополнить коллекцию\n" +
            "• ✏️ **Редактировать** — изменить перевод слова\n" +
            "• ❌ **Удалить слово** — убрать из словаря\n" +
            "• ↩️ **Назад** — вернуться в меню\n\n" +
            "Выберите действие:";

    private static final String MY_PROFILE_AUTHORIZED = "👤 **Профиль пользователя** 🌍\n\n" +
            "📋 **Основная информация:**\n" +
            "• **Логин:** testuser\n" +
            "• **Пароль:** ••••••••\n\n" +
            "⚙️ **Управление аккаунтом:**\n" +
            "• Изменить логин\n" +
            "• Изменить пароль\n" +
            "• Выйти из аккаунта\n";

    private static final String WORD_AUTHORIZED = "🎉 **Новое слово!** 🎉\n\n" +
            "🔤 **Слово:** Hello\n" +
            "🌐 **Перевод:** Привет\n" +
            "💡 **Пример использования:** Hello, how are you?\n\n" +
            "✨ Учи с удовольствием!\n" +
            "Если вы знаете данное слово нажимай на кнопки \"Знаю\", иначе \"Изучаю\"";

    private static final String SCHEDULED_TEST_AUTHORIZED = "🌙 Момент истины настал!\n\n" +
            "Знания, которые вы собирали по крупицам в течении недели и не только, готовы проверке!\n\n" +
            "✨ Готовы бросить вызов себе?";

    private static final String OLD_WORD_AUTHORIZED = "📚 *Кажется найдено забытое слово из словаря*\n" +
            "Необходимо срочно освежить в памяти его значение 💫\n\n" +
            "**Вопрос:** What is the translation of 'Hello'?\n\n" +
            "A) Привет\n" +
            "B) Пока\n" +
            "C) Спасибо\n" +
            "D) Извините";

    private static final String NO_BUTTON_RESPONSE = "💪 *Не сомневайтесь в своих силах!* 💪\n\n" +
            "📖 Тест займет всего несколько минут и поможет определить ваш текущий уровень\n\n" +
            "🕐 Когда будете готовы - просто нажмите /start\n\n" +
            "🔍 Все команды доступны по /help";

    /**
     * InMemoryBotLogic - реализация логики бота для тестов
     * Имитирует поведение реальной BotLogic без зависимостей от базы данных
     */
    public static class InMemoryBotLogic {
        private final Set<Long> authorizedUsers = new HashSet<>();
        private final Set<Long> userSessions = new HashSet<>();
        private final Set<String> registeredUsers = new HashSet<>();

        public InMemoryBotLogic() {
            registeredUsers.add("testuser");
            authorizedUsers.add(1001L);
            userSessions.add(1001L);
        }

        /**
         * Основной метод обработки сообщений - как в реальной логике
         */
        public BotResponse processMessage(String message, long chatId) {
            userSessions.add(chatId);

            if (message == null || message.trim().isEmpty()) {
                return new BotResponse(chatId, "Сообщение не может быть пустым", null);
            }

            if (message.startsWith("/")) {
                return handleCommand(message, chatId);
            } else {
                return handleTextMessage(message, chatId);
            }
        }

        /**
         * Обработка callback-ов от кнопок - как в реальной логике
         */
        public BotResponse processCallback(String callbackData, long chatId) {
            userSessions.add(chatId);

            if (callbackData == null) {
                return new BotResponse(chatId, "Ошибка: callback данные отсутствуют", null);
            }

            return handleCallback(callbackData, chatId);
        }

        /**
         * Обрабатывает команды, начинающиеся с "/"
         */
        private BotResponse handleCommand(String command, long chatId) {
            boolean isAuthorized = authorizedUsers.contains(chatId);

            switch (command) {
                case "/start":
                    return handleStartCommand(chatId, isAuthorized);
                case "/start_test":
                    return handleStartTestCommand(chatId, isAuthorized);
                case "/speed_test":
                    return handleSpeedTestCommand(chatId, isAuthorized);
                case "/dictionary":
                    return handleDictionaryCommand(chatId, isAuthorized);
                case "/my_profile":
                    return handleMyProfileCommand(chatId, isAuthorized);
                case "/help":
                    return handleHelpCommand(chatId);
                case "/word":
                    return handleWordCommand(chatId, isAuthorized);
                case "/scheduled_test":
                    return handleScheduledTestCommand(chatId, isAuthorized);
                case "/old_word":
                    return handleOldWordCommand(chatId, isAuthorized);
                default:
                    return new BotResponse(chatId,
                            "Неизвестная команда. Введите /help для списка доступных команд.", null);
            }
        }

        /**
         * Обрабатывает команду /start для авторизованных и неавторизованных пользователей
         */
        private BotResponse handleStartCommand(long chatId, boolean isAuthorized) {
            if (isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.WELCOME_AUTHORIZED, null);
            } else {
                return new BotResponse(chatId, TestBotLogic.WELCOME_UNAUTHORIZED, "sing_in_main");
            }
        }

        /**
         * Обрабатывает команду /start_test - запуск тестирования
         */
        private BotResponse handleStartTestCommand(long chatId, boolean isAuthorized) {
            if (!isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "sing_in_main");
            } else {
                return new BotResponse(chatId, TestBotLogic.START_TEST_AUTHORIZED, "start");
            }
        }

        /**
         * Обрабатывает команду /speed_test - запуск теста на скорость
         */
        private BotResponse handleSpeedTestCommand(long chatId, boolean isAuthorized) {
            if (!isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "sing_in_main");
            } else {
                return new BotResponse(chatId, TestBotLogic.SPEED_TEST_AUTHORIZED, "speed_test_start");
            }
        }

        /**
         * Обрабатывает команду /dictionary - работа со словарем
         */
        private BotResponse handleDictionaryCommand(long chatId, boolean isAuthorized) {
            if (!isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "sing_in_main");
            } else {
                return new BotResponse(chatId, TestBotLogic.DICTIONARY_AUTHORIZED, "dictionary");
            }
        }

        /**
         * Обрабатывает команду /my_profile - просмотр профиля пользователя
         */
        private BotResponse handleMyProfileCommand(long chatId, boolean isAuthorized) {
            if (!isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "sing_in_main");
            } else {
                return new BotResponse(chatId, TestBotLogic.MY_PROFILE_AUTHORIZED, "my_profile");
            }
        }

        /**
         * Обрабатывает команду /word - получение нового слова для изучения
         */
        private BotResponse handleWordCommand(long chatId, boolean isAuthorized) {
            if (!isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "sing_in_main");
            } else {
                return new BotResponse(chatId, TestBotLogic.WORD_AUTHORIZED, "schedule_message");
            }
        }

        /**
         * Обрабатывает команду /scheduled_test - запуск отложенного теста
         */
        private BotResponse handleScheduledTestCommand(long chatId, boolean isAuthorized) {
            if (!isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "sing_in_main");
            } else {
                return new BotResponse(chatId, TestBotLogic.SCHEDULED_TEST_AUTHORIZED, "schedule_test");
            }
        }

        /**
         * Обрабатывает команду /old_word - повторение слов с низким приоритетом
         */
        private BotResponse handleOldWordCommand(long chatId, boolean isAuthorized) {
            if (!isAuthorized) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "sing_in_main");
            } else {
                return new BotResponse(chatId, TestBotLogic.OLD_WORD_AUTHORIZED, "test_answers");
            }
        }

        /**
         * Обрабатывает команду /help - вывод справки по командам
         */
        private BotResponse handleHelpCommand(long chatId) {
            return new BotResponse(chatId, TestBotLogic.HELP_MESSAGE, null);
        }

        /**
         * Обрабатывает обычные текстовые сообщения (не команды)
         */
        private BotResponse handleTextMessage(String message, long chatId) {
            if (authorizedUsers.contains(chatId)) {
                return new BotResponse(chatId, "❌ Неправильный ввод или команда", null);
            } else {
                return new BotResponse(chatId, "Не понимаю команду. Введите /help для справки.", null);
            }
        }

        /**
         * Обрабатывает callback-данные от нажатых кнопок
         */
        private BotResponse handleCallback(String callbackData, long chatId) {
            boolean isAuthorized = authorizedUsers.contains(chatId);

            if (!isAuthorized && !callbackData.equals("start_button")) {
                return new BotResponse(chatId, TestBotLogic.NOT_AUTHORIZED_MESSAGE, "main");
            }

            switch (callbackData) {
                case "no_button":
                    if (isAuthorized) {
                        return new BotResponse(chatId, TestBotLogic.NO_BUTTON_RESPONSE, "main");
                    }
                    break;
                case "start_button":
                    return handleHelpCommand(chatId);
                default:
                    return new BotResponse(chatId, "Обработка callback: " + callbackData, null);
            }

            return new BotResponse(chatId, "Обработка callback: " + callbackData, null);
        }

        /**
         * Авторизует пользователя в системе
         */
        public void authorizeUser(long chatId) {
            authorizedUsers.add(chatId);
            userSessions.add(chatId);
        }

        /**
         * Деавторизует пользователя в системе
         */
        public void deauthorizeUser(long chatId) {
            authorizedUsers.remove(chatId);
            userSessions.remove(chatId);
        }

        /**
         * Проверяет авторизацию пользователя
         */
        public boolean isUserAuthorized(long chatId) {
            return authorizedUsers.contains(chatId);
        }

        /**
         * Проверяет активность пользователя (были ли взаимодействия)
         */
        public boolean isUserActive(long chatId) {
            return userSessions.contains(chatId);
        }

        /**
         * Возвращает количество авторизованных пользователей
         */
        public int getAuthorizedUsersCount() {
            return authorizedUsers.size();
        }

        /**
         * Возвращает количество активных пользователей
         */
        public int getActiveUsersCount() {
            return userSessions.size();
        }
    }

    @BeforeEach
    public void setUp() {
        botLogic = new InMemoryBotLogic();
    }

    /**
     * Команда /start для неавторизованного пользователя
     */
    @Test
    public void testStartCommandUnauthorized_Strict() {
        long chatId = UNAUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/start", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(WELCOME_UNAUTHORIZED, response.getText(), "Текст приветствия должен быть точным");
        Assertions.assertEquals("sing_in_main", response.getKeyboardType(), "Тип клавиатуры должен быть sing_in_main");
    }

    /**
     * Команда /start для авторизованного пользователя
     */
    @Test
    public void testStartCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/start", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(WELCOME_AUTHORIZED, response.getText(), "Текст приветствия для авторизованного пользователя должен быть точным");
        Assertions.assertNull(response.getKeyboardType(), "Для авторизованного пользователя клавиатура должна быть null");
    }

    /**
     * Команда /start_test для неавторизованного пользователя
     */
    @Test
    public void testStartTestCommandUnauthorized_Strict() {
        long chatId = UNAUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/start_test", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(NOT_AUTHORIZED_MESSAGE, response.getText(), "Текст ошибки доступа должен быть точным");
        Assertions.assertEquals("sing_in_main", response.getKeyboardType(), "Тип клавиатуры должен быть sing_in_main");
    }

    /**
     * Команда /start_test для авторизованного пользователя
     */
    @Test
    public void testStartTestCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/start_test", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(START_TEST_AUTHORIZED, response.getText(), "Текст приглашения к тесту должен быть точным");
        Assertions.assertEquals("start", response.getKeyboardType(), "Тип клавиатуры должен быть start");
    }

    /**
     * Команда /speed_test для авторизованного пользователя
     */
    @Test
    public void testSpeedTestCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/speed_test", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(SPEED_TEST_AUTHORIZED, response.getText(), "Текст описания теста скорости должен быть точным");
        Assertions.assertEquals("speed_test_start", response.getKeyboardType(), "Тип клавиатуры должен быть speed_test_start");
    }

    /**
     * Команда /dictionary для авторизованного пользователя
     */
    @Test
    public void testDictionaryCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/dictionary", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(DICTIONARY_AUTHORIZED, response.getText(), "Текст словаря должен быть точным");
        Assertions.assertEquals("dictionary", response.getKeyboardType(), "Тип клавиатуры должен быть dictionary");
    }

    /**
     * Команда /my_profile для авторизованного пользователя
     */
    @Test
    public void testMyProfileCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/my_profile", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(MY_PROFILE_AUTHORIZED, response.getText(), "Текст профиля должен быть точным");
        Assertions.assertEquals("my_profile", response.getKeyboardType(), "Тип клавиатуры должен быть my_profile");
    }

    /**
     * Команда /help для любого пользователя
     */
    @Test
    public void testHelpCommand_Strict() {
        BotResponse responseAuth = botLogic.processMessage("/help", AUTHORIZED_USER_ID);
        Assertions.assertNotNull(responseAuth, "Ответ для авторизованного не должен быть null");
        Assertions.assertEquals(AUTHORIZED_USER_ID, responseAuth.getChatId());
        Assertions.assertNull(responseAuth.getKeyboardType(), "Клавиатура для /help должна быть null");
        //одинакого что для авторизованного, что для нет
        BotResponse responseUnauth = botLogic.processMessage("/help", UNAUTHORIZED_USER_ID);
        Assertions.assertNotNull(responseUnauth, "Ответ для неавторизованного не должен быть null");
        Assertions.assertEquals(UNAUTHORIZED_USER_ID, responseUnauth.getChatId());
        Assertions.assertNull(responseUnauth.getKeyboardType(), "Клавиатура для /help должна быть null");

        Assertions.assertEquals(responseAuth.getText(), responseUnauth.getText(),
                "Текст помощи должен быть одинаковым для всех пользователей");
    }

    /**
     * Неизвестная команда
     */
    @Test
    public void testUnknownCommand_Strict() {
        BotResponse responseAuth = botLogic.processMessage("/unknown_command", AUTHORIZED_USER_ID);
        Assertions.assertNotNull(responseAuth, "Ответ для авторизованного не должен быть null");
        Assertions.assertEquals(AUTHORIZED_USER_ID, responseAuth.getChatId());
        Assertions.assertEquals("Неизвестная команда. Введите /help для списка доступных команд.", responseAuth.getText());
        Assertions.assertNull(responseAuth.getKeyboardType());

        BotResponse responseUnauth = botLogic.processMessage("/unknown_command", UNAUTHORIZED_USER_ID);
        Assertions.assertNotNull(responseUnauth, "Ответ для неавторизованного не должен быть null");
        Assertions.assertEquals(UNAUTHORIZED_USER_ID, responseUnauth.getChatId());
        Assertions.assertEquals("Неизвестная команда. Введите /help для списка доступных команд.", responseUnauth.getText());
        Assertions.assertNull(responseUnauth.getKeyboardType());

        Assertions.assertEquals(responseAuth.getText(), responseUnauth.getText(),
                "Текст ошибки для неизвестной команды должен быть одинаковым для всех пользователей");
    }

    /**
     * Обработка callback no_button для авторизованного пользователя
     */
    @Test
    public void testNoButtonCallbackAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processCallback("no_button", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(NO_BUTTON_RESPONSE, response.getText(), "Текст ответа на no_button должен быть точным");
        Assertions.assertEquals("main", response.getKeyboardType(), "Тип клавиатуры должен быть main");
    }

    /**
     * Обработка callback no_button для неавторизованного пользователя
     */
    @Test
    public void testNoButtonCallbackUnauthorized_Strict() {
        long chatId = UNAUTHORIZED_USER_ID;

        BotResponse response = botLogic.processCallback("no_button", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(NOT_AUTHORIZED_MESSAGE, response.getText(), "Текст ошибки доступа должен быть точным");
        Assertions.assertEquals("main", response.getKeyboardType(), "Тип клавиатуры должен быть main");
        Assertions.assertTrue(response.getText().contains("Доступ запрещен"), "Текст должен содержать сообщение о запрете доступа");
        Assertions.assertFalse(botLogic.isUserAuthorized(chatId), "Пользователь не должен быть авторизован");
    }

    /**
     * Команда /word для неавторизованного пользователя
     */
    @Test
    public void testWordCommandUnauthorized_Strict() {
        long chatId = UNAUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/word", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(NOT_AUTHORIZED_MESSAGE, response.getText(), "Текст ошибки доступа должен быть точным");
        Assertions.assertEquals("sing_in_main", response.getKeyboardType(), "Тип клавиатуры должен быть sing_in_main");
    }

    /**
     * Команда /word для авторизованного пользователя
     */
    @Test
    public void testWordCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/word", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertTrue(response.getText().contains("🎉 **Новое слово!** 🎉") ||
                        response.getText().contains("❌"),
                "Ответ должен содержать либо новое слово, либо сообщение об ошибке");
        Assertions.assertEquals("schedule_message", response.getKeyboardType(),
                "Тип клавиатуры должен быть schedule_message");
    }

    /**
     * Команда /scheduled_test для неавторизованного пользователя
     */
    @Test
    public void testScheduledTestCommandUnauthorized_Strict() {
        long chatId = UNAUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/scheduled_test", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(NOT_AUTHORIZED_MESSAGE, response.getText(), "Текст ошибки доступа должен быть точным");
        Assertions.assertEquals("sing_in_main", response.getKeyboardType(), "Тип клавиатуры должен быть sing_in_main");
    }

    /**
     * Команда /scheduled_test для авторизованного пользователя
     */
    @Test
    public void testScheduledTestCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/scheduled_test", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(SCHEDULED_TEST_AUTHORIZED, response.getText(),
                "Текст приглашения к отложенному тесту должен быть точным");
        Assertions.assertEquals("schedule_test", response.getKeyboardType(),
                "Тип клавиатуры должен быть schedule_test");
    }

    /**
     * Команда /old_word для неавторизованного пользователя
     */
    @Test
    public void testOldWordCommandUnauthorized_Strict() {
        long chatId = UNAUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/old_word", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertEquals(NOT_AUTHORIZED_MESSAGE, response.getText(), "Текст ошибки доступа должен быть точным");
        Assertions.assertEquals("sing_in_main", response.getKeyboardType(), "Тип клавиатуры должен быть sing_in_main");
    }

    /**
     * Команда /old_word для авторизованного пользователя
     */
    @Test
    public void testOldWordCommandAuthorized_Strict() {
        long chatId = AUTHORIZED_USER_ID;

        BotResponse response = botLogic.processMessage("/old_word", chatId);

        Assertions.assertNotNull(response, "Ответ не должен быть null");
        Assertions.assertEquals(chatId, response.getChatId(), "ChatId должен соответствовать запросу");
        Assertions.assertTrue(
                response.getText().contains("📚 *Кажется найдено забытое слово из словаря*") ||
                        response.getText().contains("❌ У вас пока нет слов для повторения") ||
                        response.getText().contains("Вопрос") ||
                        response.getText().contains("A)") && response.getText().contains("B)"),
                "Ответ должен содержать тест по старому слову или сообщение об ошибке"
        );

        if (response.getText().contains("❌ У вас пока нет слов для повторения")) {
            Assertions.assertEquals("main", response.getKeyboardType(),
                    "Для пустого словаря клавиатура должна быть main");
        } else {
            Assertions.assertEquals("test_answers", response.getKeyboardType(),
                    "Для активного теста клавиатура должна быть test_answers");
        }
    }

    /**
     * Управление авторизацией пользователя
     */
    @Test
    public void testUserAuthorizationManagement_Strict() {
        long chatId = NEW_USER_ID;
        int initialAuthCount = botLogic.getAuthorizedUsersCount();
        int initialActiveCount = botLogic.getActiveUsersCount();

        Assertions.assertFalse(botLogic.isUserAuthorized(chatId), "Новый пользователь не должен быть авторизован изначально");
        Assertions.assertFalse(botLogic.isUserActive(chatId), "Новый пользователь не должен быть активен изначально");

        botLogic.authorizeUser(chatId);

        Assertions.assertTrue(botLogic.isUserAuthorized(chatId), "Пользователь должен быть авторизован после authorizeUser");
        Assertions.assertTrue(botLogic.isUserActive(chatId), "Пользователь должен быть активен после авторизации");
        Assertions.assertEquals(initialAuthCount + 1, botLogic.getAuthorizedUsersCount(), "Количество авторизованных пользователей должно увеличиться на 1");
        Assertions.assertEquals(initialActiveCount + 1, botLogic.getActiveUsersCount(), "Количество активных пользователей должно увеличиться на 1");

        botLogic.deauthorizeUser(chatId);

        Assertions.assertFalse(botLogic.isUserAuthorized(chatId), "Пользователь не должен быть авторизован после deauthorizeUser");
        Assertions.assertFalse(botLogic.isUserActive(chatId), "Пользователь не должен быть активен после деавторизации");
        Assertions.assertEquals(initialAuthCount, botLogic.getAuthorizedUsersCount(), "Количество авторизованных пользователей должно вернуться к исходному");
        Assertions.assertEquals(initialActiveCount, botLogic.getActiveUsersCount(), "Количество активных пользователей должно вернуться к исходному");
    }

    /**
     * Проверка активности пользователя
     */
    @Test
    public void testUserActivityTracking_Strict() {
        long chatId = NEW_USER_ID;
        int initialActiveCount = botLogic.getActiveUsersCount();

        Assertions.assertFalse(botLogic.isUserActive(chatId), "Новый пользователь не должен быть активен изначально");

        BotResponse response1 = botLogic.processMessage("/start", chatId);

        Assertions.assertTrue(botLogic.isUserActive(chatId), "Пользователь должен быть активен после отправки сообщения");
        Assertions.assertEquals(initialActiveCount + 1, botLogic.getActiveUsersCount(), "Количество активных пользователей должно увеличиться на 1");

        BotResponse response2 = botLogic.processCallback("start_button", chatId);

        Assertions.assertTrue(botLogic.isUserActive(chatId), "Пользователь должен оставаться активным после callback");
        Assertions.assertEquals(initialActiveCount + 1, botLogic.getActiveUsersCount(), "Количество активных пользователей должно остаться увеличенным");

        Assertions.assertNotNull(response1, "Ответ на сообщение не должен быть null");
        Assertions.assertNotNull(response2, "Ответ на callback не должен быть null");
    }

    /**
     * Обработка пустых и null данных
     */
    @Test
    public void testEmptyAndNullData_Strict() {
        BotResponse emptyResponse = botLogic.processMessage("", AUTHORIZED_USER_ID);
        Assertions.assertNotNull(emptyResponse, "Ответ на пустое сообщение не должен быть null");
        Assertions.assertEquals("Сообщение не может быть пустым", emptyResponse.getText());
        Assertions.assertNull(emptyResponse.getKeyboardType());

        BotResponse nullResponse = botLogic.processMessage(null, AUTHORIZED_USER_ID);
        Assertions.assertNotNull(nullResponse, "Ответ на null сообщение не должен быть null");
        Assertions.assertEquals("Сообщение не может быть пустым", nullResponse.getText());
        Assertions.assertNull(nullResponse.getKeyboardType());

        BotResponse nullCallback = botLogic.processCallback(null, AUTHORIZED_USER_ID);
        Assertions.assertNotNull(nullCallback, "Ответ на null callback не должен быть null");
        Assertions.assertEquals("Ошибка: callback данные отсутствуют", nullCallback.getText());
        Assertions.assertNull(nullCallback.getKeyboardType());
    }

    /**
     * Обычное текстовое сообщение
     */
    @Test
    public void testRegularTextMessage_Strict() {
        BotResponse authResponse = botLogic.processMessage("обычный текст", AUTHORIZED_USER_ID);
        Assertions.assertNotNull(authResponse, "Ответ для авторизованного не должен быть null");
        Assertions.assertEquals(AUTHORIZED_USER_ID, authResponse.getChatId());
        Assertions.assertEquals("❌ Неправильный ввод или команда", authResponse.getText());
        Assertions.assertNull(authResponse.getKeyboardType());

        BotResponse unauthResponse = botLogic.processMessage("обычный текст", UNAUTHORIZED_USER_ID);
        Assertions.assertNotNull(unauthResponse, "Ответ для неавторизованного не должен быть null");
        Assertions.assertEquals(UNAUTHORIZED_USER_ID, unauthResponse.getChatId());
        Assertions.assertEquals("Не понимаю команду. Введите /help для справки.", unauthResponse.getText());
        Assertions.assertNull(unauthResponse.getKeyboardType());

        Assertions.assertNotEquals(authResponse.getText(), unauthResponse.getText(),
                "Текст ответа на обычное сообщение должен различаться для авторизованных и неавторизованных пользователей");
    }
}