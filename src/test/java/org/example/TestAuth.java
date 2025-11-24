package org.example;

import org.example.Authentication.AuthCommand;
import org.example.Authentication.AuthService;
import org.example.Data.BotResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TestAuth - тестирует функциональность работы с аутентификацией: регистрацию, вход,
 * изменение данных пользователя и управление профилем.
 * Использует in-memory реализацию для изоляции тестов.
 */
public class TestAuth {

    /**
     * MockAuthService — in-memory реализация AuthService,
     * используемая в тестах для управления пользователями без реальной БД.
     */
    public class MockAuthService implements AuthService {
        /**
         * Внутреннее представление пользователя в моковой базе.
         */
        private class AuthUser {
            String originalUsername;
            String currentUsername;
            String passwordHash;

            AuthUser(String username, String passwordHash) {
                this.originalUsername = username;
                this.currentUsername = username;
                this.passwordHash = passwordHash;
            }
        }

        private final Map<String, AuthUser> users = new HashMap<>();
        private final Map<Long, String> telegramChats = new HashMap<>();
        private final Map<Long, String> discordChannels = new HashMap<>();

        public MockAuthService() {
            // Мок в оперативной памяти; файл базы данных не создается
        }

        @Override
        public boolean registerUser(String username, String password) {
            if (users.containsKey(username)) {
                return false;
            }

            String passwordHash = hashPassword(password, username);
            users.put(username, new AuthUser(username, passwordHash));
            return true;
        }

        @Override
        public boolean authenticate(String username, String password) {
            AuthUser user = users.get(username);
            if (user == null) {
                return false;
            }

            String computedHash = hashPassword(password, user.originalUsername);
            return user.passwordHash.equals(computedHash);
        }

        @Override
        public boolean resetPassword(String username, String newPassword) {
            AuthUser user = users.get(username);
            if (user == null) {
                return false;
            }

            user.passwordHash = hashPassword(newPassword, user.originalUsername);
            return true;
        }

        @Override
        public boolean changeUsername(String oldUsername, String newUsername) {
            if (users.containsKey(newUsername)) {
                return false;
            }

            AuthUser user = users.remove(oldUsername);
            if (user == null) {
                return false;
            }

            user.currentUsername = newUsername;
            users.put(newUsername, user);

            // Обновляем привязки чатов
            telegramChats.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(oldUsername))
                    .forEach(entry -> entry.setValue(newUsername));

            discordChannels.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(oldUsername))
                    .forEach(entry -> entry.setValue(newUsername));

            return true;
        }

        @Override
        public String getOriginalUsername(String currentUsername) {
            AuthUser user = users.get(currentUsername);
            return user != null ? user.originalUsername : null;
        }

        @Override
        public boolean linkTelegramChat(String username, long telegramChatId) {
            if (!users.containsKey(username)) {
                return false;
            }
            telegramChats.put(telegramChatId, username);
            return true;
        }

        @Override
        public boolean linkDiscordChannel(String username, long discordChannelId) {
            if (!users.containsKey(username)) {
                return false;
            }
            discordChannels.put(discordChannelId, username);
            return true;
        }

        @Override
        public boolean isTelegramUserAuthorized(long telegramChatId) {
            return telegramChats.containsKey(telegramChatId);
        }

        @Override
        public boolean isDiscordUserAuthorized(long discordChannelId) {
            return discordChannels.containsKey(discordChannelId);
        }

        @Override
        public String getUsernameByTelegramChatId(long telegramChatId) {
            return telegramChats.get(telegramChatId);
        }

        @Override
        public String getUsernameByDiscordChannelId(long discordChannelId) {
            return discordChannels.get(discordChannelId);
        }

        @Override
        public boolean unlinkCurrentChat(long chatId) {
            boolean telegramUnlinked = telegramChats.remove(chatId) != null;
            boolean discordUnlinked = discordChannels.remove(chatId) != null;
            return telegramUnlinked || discordUnlinked;
        }

        @Override
        public Set<Long> getAllTelegramUsers() {
            return new HashSet<>(telegramChats.keySet());
        }

        @Override
        public Set<Long> getAllDiscordUsers() {
            return new HashSet<>(discordChannels.keySet());
        }

        /**
         * Вспомогательный метод для хеширования пароля
         */
        private String hashPassword(String password, String username) {
            // Упрощенная реализация хеширования для тестов
            return password + "_hash_" + username;
        }

        /**
         * Вспомогательный метод для получения количества пользователей
         */
        public int getUserCount() {
            return users.size();
        }

        /**
         * Вспомогательный метод для авторизации пользователя в тестах BotLogic
         */
        public void authorizeUser(long chatId, String username, String password) {
            registerUser(username, password);
            linkTelegramChat(username, chatId);
        }

        /**
         * Вспомогательный метод для деавторизации пользователя
         */
        public void deauthorizeUser(long chatId) {
            telegramChats.remove(chatId);
            discordChannels.remove(chatId);
        }
    }

    private AuthCommand authCommand;
    private MockAuthService mockAuthService;

    @BeforeEach
    public void setUp() {
        mockAuthService = new MockAuthService();
        authCommand = new AuthCommand(mockAuthService);
    }

    // ТЕСТЫ АУТЕНТИФИКАЦИИ (AuthCommand и AuthService)
    /**
     * Тест: регистрация нового пользователя
     */
    @Test
    public void testUserRegistration() {
        long chatId = 100L;

        // Начинаем процесс регистрации
        authCommand.handleButtonClick("reg_button", chatId, true);

        // Вводим логин
        String loginResponse = authCommand.handleTextMessage("testuser", chatId, true);
        Assertions.assertEquals("✅ **Отлично! Вы ввели логин:** testuser 👍\n\n🔒 **Теперь придумайте и введите пароль:** ✍️", loginResponse);

        // Вводим пароль
        String passwordResponse = authCommand.handleTextMessage("testpass", chatId, true);
        Assertions.assertEquals("✅ **Регистрация завершена!** 🎉\n\n**Ваши данные:**\n👤 Логин: testuser  \n🔑 Пароль: testpass\n\n**Теперь войдите в свой профиль** 🔐", passwordResponse);

        // Проверяем, что пользователь создан
        Assertions.assertEquals(1, mockAuthService.getUserCount());
        Assertions.assertEquals(true, mockAuthService.authenticate("testuser", "testpass"));
    }

    /**
     * Тест: вход существующего пользователя
     */
    @Test
    public void testUserLogin() {
        long chatId = 101L;

        // Сначала регистрируем пользователя
        mockAuthService.registerUser("existinguser", "existingpass");
        mockAuthService.linkTelegramChat("existinguser", chatId);

        // Начинаем процесс входа
        authCommand.handleButtonClick("sing_in_button", chatId, true);

        // Вводим логин
        String loginResponse = authCommand.handleTextMessage("existinguser", chatId, true);
        Assertions.assertEquals("👤 **Ваш логин:** existinguser ✅\n🔒 **Теперь введите ваш пароль:**\n⬇️ *Отправьте пароль сообщением*", loginResponse);

        // Вводим пароль
        String passwordResponse = authCommand.handleTextMessage("existingpass", chatId, true);
        Assertions.assertEquals("🎉 **Отлично! Вход выполнен!**\n\n" +
                "Привет, **existinguser**! ✨  \n" +
                "GlobeTalk снова готов помочь тебе с языками!\n\n" +
                "📚 **Выбери, чем хочешь заняться:**\n" +
                "• Попрактиковать слова\n" +
                "• Пройти тест\n" +
                "• Пополнить словарь\n\n" +
                "Ежедневно для обучения я буду присылать вам новое слово или фразу!\n" +
                "Готов учиться? 😊\n" +
                "⬇️Все разделы сбоку", passwordResponse);
    }

    /**
     * Тест: попытка входа с неверным паролем
     */
    @Test
    public void testLoginWithWrongPassword() {
        long chatId = 102L;

        // Регистрируем пользователя
        mockAuthService.registerUser("testuser", "correctpass");

        // Начинаем процесс входа
        authCommand.handleButtonClick("sing_in_button", chatId, true);
        authCommand.handleTextMessage("testuser", chatId, true);

        // Вводим неверный пароль
        String response = authCommand.handleTextMessage("wrongpass", chatId, true);
        Assertions.assertEquals("🔐 **Кажется, у нас проблемка...** 😕\n\nТо ли пароль неверный, то ли логин, \nа может, вы вообще не зарегистрировались?\n\n🔄 **Попробуйте еще раз** или \n📝 **зарегистрируйтесь**, если у вас еще нет аккаунта", response);
    }

    /**
     * Тест: попытка регистрации с занятым логином
     */
    @Test
    public void testRegistrationWithExistingUsername() {
        long chatId = 103L;

        // Регистрируем первого пользователя
        mockAuthService.registerUser("existing", "pass1");

        // Пытаемся зарегистрировать второго с тем же логином
        authCommand.handleButtonClick("reg_button", chatId, true);
        authCommand.handleTextMessage("existing", chatId, true);

        String response = authCommand.handleTextMessage("pass2", chatId, true);
        Assertions.assertEquals("❌ **Ошибка регистрации!**\nЛогин уже занят или произошла ошибка", response);
    }

    /**
     * Тест: изменение логина пользователя
     */
    @Test
    public void testChangeUsername() {
        long chatId = 104L;

        // Регистрируем пользователя
        mockAuthService.registerUser("olduser", "password");
        mockAuthService.linkTelegramChat("olduser", chatId);

        // Начинаем процесс изменения логина
        authCommand.handleButtonClick("login_edit_button", chatId, true);

        // Вводим новый логин
        String response = authCommand.handleTextMessage("newuser", chatId, true);
        Assertions.assertEquals("✅ **Логин изменен!**\nНовый логин: newuser", response);

        // Проверяем, что логин изменился
        Assertions.assertEquals("newuser", mockAuthService.getUsernameByTelegramChatId(chatId));
        Assertions.assertEquals(true, mockAuthService.authenticate("newuser", "password"));
    }

    /**
     * Тест: изменение пароля пользователя
     */
    @Test
    public void testChangePassword() {
        long chatId = 105L;

        // Регистрируем пользователя
        mockAuthService.registerUser("testuser", "oldpass");
        mockAuthService.linkTelegramChat("testuser", chatId);

        // Начинаем процесс изменения пароля
        authCommand.handleButtonClick("password_edit_button", chatId, true);

        // Вводим новый пароль
        String response = authCommand.handleTextMessage("newpass", chatId, true);
        Assertions.assertEquals("✅ **Пароль изменен!**", response);

        // Проверяем, что пароль изменился
        Assertions.assertEquals(true, mockAuthService.authenticate("testuser", "newpass"));
        Assertions.assertEquals(false, mockAuthService.authenticate("testuser", "oldpass"));
    }

    /**
     * Тест: выход из аккаунта
     */
    @Test
    public void testLogout() {
        long chatId = 106L;

        // Регистрируем и входим пользователя
        mockAuthService.registerUser("logoutuser", "password");
        mockAuthService.linkTelegramChat("logoutuser", chatId);

        // Начинаем процесс выхода
        authCommand.handleButtonClick("log_out_button", chatId, true);
        String response = authCommand.handleButtonClick("log_out_final_button", chatId, true);

        Assertions.assertEquals("👋 **Вы вышли из аккаунта**\n\nАккаунт: **logoutuser**  \nСессия завершена.\n\nЧтобы снова получить доступ к вашему профилю, выполните вход.\n\n🌍 *Ждем вас снова в GlobeTalk!*", response);

        // Проверяем, что чат отвязан
        Assertions.assertEquals(false, mockAuthService.isTelegramUserAuthorized(chatId));
    }

    /**
     * Тест: отмена выхода из аккаунта
     */
    @Test
    public void testCancelLogout() {
        long chatId = 107L;

        // Регистрируем пользователя
        mockAuthService.registerUser("canceluser", "password");
        mockAuthService.linkTelegramChat("canceluser", chatId);

        // Начинаем процесс выхода и отменяем
        authCommand.handleButtonClick("log_out_button", chatId, true);
        String response = authCommand.handleButtonClick("log_out_cancel_button", chatId, true);

        // Проверяем, что остались в системе
        Assertions.assertEquals(true, mockAuthService.isTelegramUserAuthorized(chatId));
    }

    /**
     * Тест: получение профиля пользователя
     */
    @Test
    public void testGetUserProfile() {
        long chatId = 108L;

        // Регистрируем пользователя
        mockAuthService.registerUser("profileuser", "password");
        mockAuthService.linkTelegramChat("profileuser", chatId);

        String response = authCommand.getUserProfileMessage(chatId);
        Assertions.assertEquals("👤 **Профиль пользователя** 🌍\n\n" +
                "📋 **Основная информация:**\n" +
                "• **Логин:** profileuser\n" +
                "• **Пароль:** ••••••••\n\n" +
                "⚙️ **Управление аккаунтом:**\n" +
                "• Изменить логин\n" +
                "• Изменить пароль\n" +
                "• Выйти из аккаунта\n", response);
    }

    /**
     * Тест: получение профиля неавторизованного пользователя
     */
    @Test
    public void testGetUserProfileUnauthorized() {
        long chatId = 109L;

        String response = authCommand.getUserProfileMessage(chatId);
        Assertions.assertEquals("❌ **Пользователь не найден!**\nСначала войдите в аккаунт", response);
    }

    /**
     * Тест: стартовое сообщение для авторизованного пользователя
     */
    @Test
    public void testStartMessageAuthorized() {
        long chatId = 110L;

        // Регистрируем пользователя
        mockAuthService.registerUser("authuser", "password");
        mockAuthService.linkTelegramChat("authuser", chatId);

        String response = authCommand.getStartMessage(chatId);
        Assertions.assertEquals("🌍 *С возвращением в GlobeTalk!* 🌍\n\nРады снова видеть вас! Ваш персональный помощник в изучении иностранных языков готов к работе! 🎯\n\n✨ **Ваш аккаунт активен, доступ открыт:**\n• Продолжайте обучение по персональной программе\n• Доступ к урокам и упражнениям\n• Ваш личный словарь\n• Трекинг прогресса\n\n📚 **Что хотите сделать?**\n• Продолжить тестирование\n• Попрактиковать слова из словаря\n• Пройти новые упражнения\n\n🎯 **Продолжайте изучать языки!**\n🚀 Выберите действие из меню", response);
    }

    /**
     * Тест: стартовое сообщение для неавторизованного пользователя
     */
    @Test
    public void testStartMessageUnauthorized() {
        long chatId = 111L;

        String response = authCommand.getStartMessage(chatId);
        Assertions.assertEquals("🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\nВаш персональный помощник в изучении иностранных языков! 🎯\n\n📝 **Для начала работы необходимо зарегистрироваться**\nЭто займет всего 30 секунд, но откроет все возможности платформы!\n\n✨ **После регистрации вы получите:**\n• Персональную программу обучения\n• Доступ к урокам и упражнениям\n• Доступ к созданию личного словаря\n• Трекинг прогресса\n\n📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n🎯 **Готовы открыть мир языков?**\n🚀 Начните с регистрации и тестирования!", response);
    }

    /**
     * Проверка обработки неизвестной команды
     */
    @Test
    public void testUnknownCommand() {
        long chatId = 112L;

        String response = authCommand.handleButtonClick("unknown_command", chatId, true);
        Assertions.assertEquals("❌ Неизвестная команда аутентификации", response);
    }

    /**
     * Проверка очистки состояния пользователя
     */
    @Test
    public void testClearUserState() {
        long chatId = 113L;

        // Устанавливаем состояние
        authCommand.handleButtonClick("reg_button", chatId, true);

        // Очищаем состояние
        authCommand.clearUserState(chatId);

        // Проверяем, что состояние очищено (должен вернуться стартовый текст)
        String response = authCommand.handleTextMessage("test", chatId, true);
        Assertions.assertEquals("🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\nВаш персональный помощник в изучении иностранных языков! 🎯\n\n📝 **Для начала работы необходимо зарегистрироваться**\nЭто займет всего 30 секунд, но откроет все возможности платформы!\n\n✨ **После регистрации вы получите:**\n• Персональную программу обучения\n• Доступ к урокам и упражнениям\n• Доступ к созданию личного словаря\n• Трекинг прогресса\n\n📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n🎯 **Готовы открыть мир языков?**\n🚀 Начните с регистрации и тестирования!", response);
    }

    /**
     * Тест: получение всех Telegram пользователей
     */
    @Test
    public void testGetAllTelegramUsers() {
        long chatId1 = 200L;
        long chatId2 = 201L;

        mockAuthService.registerUser("user1", "pass1");
        mockAuthService.registerUser("user2", "pass2");
        mockAuthService.linkTelegramChat("user1", chatId1);
        mockAuthService.linkTelegramChat("user2", chatId2);

        Set<Long> telegramUsers = mockAuthService.getAllTelegramUsers();
        Assertions.assertEquals(2, telegramUsers.size());
        Assertions.assertEquals(true, telegramUsers.contains(chatId1));
        Assertions.assertEquals(true, telegramUsers.contains(chatId2));
    }

    /**
     * Тест: получение всех Discord пользователей
     */
    @Test
    public void testGetAllDiscordUsers() {
        long channelId1 = 300L;
        long channelId2 = 301L;

        mockAuthService.registerUser("user1", "pass1");
        mockAuthService.registerUser("user2", "pass2");
        mockAuthService.linkDiscordChannel("user1", channelId1);
        mockAuthService.linkDiscordChannel("user2", channelId2);

        Set<Long> discordUsers = mockAuthService.getAllDiscordUsers();
        Assertions.assertEquals(2, discordUsers.size());
        Assertions.assertEquals(true, discordUsers.contains(channelId1));
        Assertions.assertEquals(true, discordUsers.contains(channelId2));
    }

    /**
     * Тест: отвязка несуществующего чата
     */
    @Test
    public void testUnlinkNonExistentChat() {
        long chatId = 400L;

        boolean result = mockAuthService.unlinkCurrentChat(chatId);
        Assertions.assertEquals(false, result);
    }

    /**
     * Тест: получение оригинального логина
     */
    @Test
    public void testGetOriginalUsername() {
        mockAuthService.registerUser("originaluser", "password");

        String originalUsername = mockAuthService.getOriginalUsername("originaluser");
        Assertions.assertEquals("originaluser", originalUsername);
    }

    /**
     * Тест: получение оригинального логина для несуществующего пользователя
     */
    @Test
    public void testGetOriginalUsernameForNonExistentUser() {
        String originalUsername = mockAuthService.getOriginalUsername("nonexistent");
        Assertions.assertEquals(null, originalUsername);
    }
}