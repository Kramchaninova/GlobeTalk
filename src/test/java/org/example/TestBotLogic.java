package org.example;

import org.example.Data.BotResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * TestBotLogic - тестирует основную логику бота
 */
public class TestBotLogic {

    private BotLogic botLogic;
    private final long AUTHORIZED_USER_ID = 1001L;
    private final long UNAUTHORIZED_USER_ID = 1002L;

    @BeforeEach
    public void setUp() {
        botLogic = new BotLogic();

        // Регистрируем и входим пользователя
        registerAndLoginUser(AUTHORIZED_USER_ID);
    }

    /**
     * Вспомогательный метод для регистрации и входа пользователя
     */
    private void registerAndLoginUser(long chatId) {
        // Регистрация
        botLogic.processCallback("reg_button", chatId);
        botLogic.processMessage("testuser", chatId);
        botLogic.processMessage("testpass", chatId);

        // Вход
        botLogic.processCallback("sing_in_button", chatId);
        botLogic.processMessage("testuser", chatId);
        botLogic.processMessage("testpass", chatId);
    }

    /**
     * Тест: команда /start для неавторизованного пользователя
     */
    @Test
    public void testStartCommandUnauthorized() {
        BotResponse response = botLogic.processMessage("/start", UNAUTHORIZED_USER_ID);

        String expectedText = "🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
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

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("sing_in_main", response.getKeyboardType());
    }

    /**
     * Тест: команда /start для авторизованного пользователя
     */
    @Test
    public void testStartCommandAuthorized() {
        BotResponse response = botLogic.processMessage("/start", AUTHORIZED_USER_ID);

        String expectedText = "🌍 *С возвращением в GlobeTalk!* 🌍\n\n" +
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

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertNull(response.getKeyboardType());
    }

    /**
     * Тест: команда /start_test для неавторизованного пользователя
     */
    @Test
    public void testStartTestCommandUnauthorized() {
        BotResponse response = botLogic.processMessage("/start_test", UNAUTHORIZED_USER_ID);

        String expectedText = "❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.";

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("sing_in_main", response.getKeyboardType());
    }

    /**
     * Тест: команда /start_test для авторизованного пользователя
     */
    @Test
    public void testStartTestCommandAuthorized() {
        BotResponse response = botLogic.processMessage("/start_test", AUTHORIZED_USER_ID);

        String expectedText = "Ваш персональный помощник в изучении иностранных языков GlobeTalk!* 🌍!\n\n" +
                "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n" +
                "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
                "🔍 Для просмотра всех команд нажмите /help\n\n" +
                "🚀 **Вы готовы начать тест?**";

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("start", response.getKeyboardType());
    }

    /**
     * Тест: команда /speed_test для неавторизованного пользователя
     */
    @Test
    public void testSpeedTestCommandUnauthorized() {
        BotResponse response = botLogic.processMessage("/speed_test", UNAUTHORIZED_USER_ID);

        String expectedText = "❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.";

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("sing_in_main", response.getKeyboardType());
    }

    /**
     * Тест: команда /speed_test для авторизованного пользователя
     */
    @Test
    public void testSpeedTestCommandAuthorized() {
        BotResponse response = botLogic.processMessage("/speed_test", AUTHORIZED_USER_ID);

        String expectedText =  "🌍 *Добро пожаловать в тест на скорость!* 🌍\n\n" +
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

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("speed_test_start", response.getKeyboardType());
    }

    /**
     * Тест: команда /dictionary для неавторизованного пользователя
     */
    @Test
    public void testDictionaryCommandUnauthorized() {
        BotResponse response = botLogic.processMessage("/dictionary", UNAUTHORIZED_USER_ID);

        String expectedText = "❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.";

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("sing_in_main", response.getKeyboardType());
    }

    /**
     * Тест: команда /dictionary для авторизованного пользователя
     */
    @Test
    public void testDictionaryCommandAuthorized() {
        BotResponse response = botLogic.processMessage("/dictionary", AUTHORIZED_USER_ID);

        String expectedText ="✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь пуст*\nДобавьте первое слово для начала изучения!\n\n" +
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить перевод слова\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:";;

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("dictionary", response.getKeyboardType());
    }

    /**
     * Тест: команда /my_profile для неавторизованного пользователя
     */
    @Test
    public void testMyProfileCommandUnauthorized() {
        BotResponse response = botLogic.processMessage("/my_profile", UNAUTHORIZED_USER_ID);

        String expectedText = "❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.";

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("sing_in_main", response.getKeyboardType());
    }

    /**
     * Тест: команда /my_profile для авторизованного пользователя
     */
    @Test
    public void testMyProfileCommandAuthorized() {
        BotResponse response = botLogic.processMessage("/my_profile", AUTHORIZED_USER_ID);

        String expectedText = "👤 **Профиль пользователя** 🌍\n\n" +
                "📋 **Основная информация:**\n" +
                "• **Логин:** testuser\n" +
                "• **Пароль:** ••••••••\n\n" +
                "⚙️ **Управление аккаунтом:**\n" +
                "• Изменить логин\n" +
                "• Изменить пароль\n" +
                "• Выйти из аккаунта\n";

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("my_profile", response.getKeyboardType());
    }

    /**
     * Тест: команда /help для неавторизованного пользователя
     */
    @Test
    public void testHelpCommandUnauthorized() {
        BotResponse response = botLogic.processMessage("/help", UNAUTHORIZED_USER_ID);

        String expectedText = "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +
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

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertNull(response.getKeyboardType());
    }

    /**
     * Тест: команда /help для авторизованного пользователя
     */
    @Test
    public void testHelpCommandAuthorized() {
        BotResponse response = botLogic.processMessage("/help", AUTHORIZED_USER_ID);

        String expectedText = "🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +
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

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertNull(response.getKeyboardType());
    }

    /**
     * Тест: неизвестная команда для неавторизованного пользователя
     */
    @Test
    public void testUnknownCommandUnauthorized() {
        BotResponse response = botLogic.processMessage("/unknown", UNAUTHORIZED_USER_ID);

        String expectedText = "Неизвестная команда. Введите /help для списка доступных команд.";

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertNull(response.getKeyboardType());
    }

    /**
     * Тест: неизвестная команда для авторизованного пользователя
     */
    @Test
    public void testUnknownCommandAuthorized() {
        BotResponse response = botLogic.processMessage("/unknown", AUTHORIZED_USER_ID);

        String expectedText = "Неизвестная команда. Введите /help для списка доступных команд.";

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertNull(response.getKeyboardType());
    }

    /**
     * Тест: обычное текстовое сообщение для неавторизованного пользователя
     */
    @Test
    public void testRegularTextMessageUnauthorized() {
        BotResponse response = botLogic.processMessage("обычный текст", UNAUTHORIZED_USER_ID);

        String expectedText = "Не понимаю команду. Введите /help для справки.";

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertNull(response.getKeyboardType());
    }

    /**
     * Тест: обычное текстовое сообщение для авторизованного пользователя
     */
    @Test
    public void testRegularTextMessageAuthorized() {
        BotResponse response = botLogic.processMessage("обычный текст", AUTHORIZED_USER_ID);

        String expectedText = "❌ Неправильный ввод или команда";

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
    }

    /**
     * Тест: обработка кнопки "Назад" (no_button) для неавторизованного пользователя
     */
    @Test
    public void testNoButtonProcessingUnauthorized() {
        BotResponse response = botLogic.processCallback("no_button", UNAUTHORIZED_USER_ID);

        String expectedText = "❌ **Доступ запрещен!**\n\n" +
                "Для использования этой функции необходимо войти в аккаунт.\n\n" +
                "🔐 Используйте команду /start для регистрации или входа.";

        Assertions.assertEquals(UNAUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("main", response.getKeyboardType());
    }

    /**
     * Тест: обработка кнопки "Назад" (no_button) для авторизованного пользователя
     */
    @Test
    public void testNoButtonProcessingAuthorized() {
        BotResponse response = botLogic.processCallback("no_button", AUTHORIZED_USER_ID);

        String expectedText = "💪 *Не сомневайтесь в своих силах!* 💪\n\n" +
                "📖 Тест займет всего несколько минут и поможет определить ваш текущий уровень\n\n" +
                "🕐 Когда будете готовы - просто нажмите /start\n\n" +
                "🔍 Все команды доступны по /help";

        Assertions.assertEquals(AUTHORIZED_USER_ID, response.getChatId());
        Assertions.assertEquals(expectedText, response.getText());
        Assertions.assertEquals("main", response.getKeyboardType());
    }
}