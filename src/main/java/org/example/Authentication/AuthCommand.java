package org.example.Authentication;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthCommand - обрабатывает команды и кнопки аутентификации.
 * Управляет взаимодействием пользователя с системой аутентификации.
 */
public class AuthCommand {
    private final AuthService authService;

    // Состояния для входящих сообщений
    private final Map<Long, String> userStates = new HashMap<>(); // chatId -> "waiting_login", "waiting_password" и тд

    public AuthCommand(AuthService authService) {
        this.authService = authService;
    }
    private static final String START_MESSAGE_ACTIVATED = "🌍 *С возвращением в GlobeTalk!* 🌍\n\n" +
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

    private static final String START_MESSAGE = "🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
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

    private static final String REGISTRATION_MESSAGE = "📝 **Регистрация в GlobeTalk** 📝\n\n" +
            "Для создания вашего аккаунта нам потребуется:\n\n" +
            "🔸 **Логин** - ваш уникальный идентификатор в системе  \n" +
            "🔸 **Пароль** - надежная защита вашего аккаунта  \n\n" +
            "📋 **Пожалуйста, вводите данные по одному сообщением в следующем порядке:**\n\n" +
            "1️⃣ Сначала пришлите ваш **логин**  \n" +
            "2️⃣ Затем - **пароль**\n\n" +
            "⚡ *Все данные будут надежно защищены!*";

    private static final String REG_LOGIN = "✅ **Отлично! Вы ввели логин:** {username} 👍\n\n" +
            "🔒 **Теперь придумайте и введите пароль:** ✍️";

    private static final String REG_FINAL = "✅ **Регистрация завершена!** 🎉\n\n" +
            "**Ваши данные:**\n" +
            "👤 Логин: {username}  \n" +
            "🔑 Пароль: {password}\n\n" +
            "**Теперь войдите в свой профиль** 🔐";

    private static final String SING_IN = "🔐 **Вход в аккаунт GlobeTalk**\n\n" +
            "Для входа в ваш профиль пожалуйста:\n\n" +
            "1️⃣ Введите ваш **логин**\n" +
            "2️⃣ Затем введите **пароль**";

    private static final String SING_IN_LOGIN = "👤 **Ваш логин:** {username} ✅\n" +
            "🔒 **Теперь введите ваш пароль:**\n" +
            "⬇️ *Отправьте пароль сообщением*";

    private static final String SING_IN_ERROR = "🔐 **Кажется, у нас проблемка...** 😕\n\n" +
            "То ли пароль неверный, то ли логин, \n" +
            "а может, вы вообще не зарегистрировались?\n\n" +
            "🔄 **Попробуйте еще раз** или \n" +
            "📝 **зарегистрируйтесь**, если у вас еще нет аккаунта";

    private static final String SING_IN_SUCCESSFUL = "🎉 **Отлично! Вход выполнен!**\n\n" +
            "Привет, **{username}**! ✨  \n" +
            "GlobeTalk снова готов помочь тебе с языками!\n\n" +
            "📚 **Выбери, чем хочешь заняться:**\n" +
            "• Попрактиковать слова\n" +
            "• Пройти тест\n" +
            "• Пополнить словарь\n\n" +
            "Ежедневно для обучения я буду присылать вам новое слово или фразу!\n" +
            "Готов учиться? 😊\n"+
            "⬇️Все разделы сбоку";

    private static final String LOGIN_EDIT ="✏️ **Изменение логина**\n" +
            "Текущий логин: **{username}**\n" +
            "Введите новый логин:";

    private static final String PASSWORD_EDIT = "🔐 **Смена пароля**\n" +
            "Введите новый пароль:";

    private static final String LOG_OUT_CONFIRM = "🚪 **Выход из аккаунта**\n" +
            "Вы действительно хотите выйти?";

    private static final String LOG_OUT_FINAL = "👋 **Вы вышли из аккаунта**\n\n" +
            "Аккаунт: **{username}**  \n" +
            "Сессия завершена.\n\n" +
            "Чтобы снова получить доступ к вашему профилю, выполните вход.\n\n" +
            "🌍 *Ждем вас снова в GlobeTalk!*";

    /**
     * Получает username по chatId (работает для Telegram и Discord)
     */
    private String getUsernameByChatId(long chatId) {
        // Сначала проверяем Telegram, потом Discord
        String username = authService.getUsernameByTelegramChatId(chatId);
        if (username == null) {
            username = authService.getUsernameByDiscordChannelId(chatId);
        }
        return username;
    }
    /**
     * Возвращает стартовое сообщение в зависимости от авторизации
     */
    public String getStartMessage(long chatId) {
        // Проверяем авторизацию для Telegram и Discord
        boolean isAuthorized = authService.isTelegramUserAuthorized(chatId) ||
                authService.isDiscordUserAuthorized(chatId);

        if (isAuthorized) {
            System.out.println("[Auth Command] Пользователь авторизован, показываем активированное сообщение");
            return START_MESSAGE_ACTIVATED;
        } else {
            System.out.println("[Auth Command] Пользователь не авторизован, показываем стандартное сообщение");
            return START_MESSAGE;
        }
    }

    /**
     * Возвращает стартовое сообщение
     */
    public String getStartMessage() {
        return START_MESSAGE;
    }

    /**
     * Возвращает сообщение с профилем пользователя
     */
    public String getUserProfileMessage(long chatId) {
        String username = getUsernameByChatId(chatId);

        if (username == null) {
            return "❌ **Пользователь не найден!**\nСначала войдите в аккаунт";
        }

        return "👤 **Профиль пользователя** 🌍\n\n" +
                "📋 **Основная информация:**\n" +
                "• **Логин:** " + username + "\n" +
                "• **Пароль:** ••••••••\n\n" +
                "⚙️ **Управление аккаунтом:**\n" +
                "• Изменить логин\n"+
                "• Изменить пароль\n"+
                "• Выйти из аккаунта\n";
    }

    /**
     * Обрабатывает нажатия кнопок аутентификации
     */
    public String handleButtonClick(String callbackData, long chatId, boolean isTelegram) {
        System.out.println("[Auth Command] Обработка кнопки: " + callbackData + " для chatId: " + chatId);

        switch (callbackData) {
            case "start_button":
                return getStartMessage();
            case "log_out_cancel_button":
                return getStartMessage(chatId);
            case "my_profile_button":
                return getUserProfileMessage(chatId);

            case "sing_in_button", "login_again_button":
                userStates.put(chatId, "waiting_sing_in_login");
                System.out.println("[Auth Command] Установлено состояние: waiting_sing_in_login");
                return SING_IN;

            case "reg_button":
                userStates.put(chatId, "waiting_reg_login");
                System.out.println("[Auth Command] Установлено состояние: waiting_reg_login");
                return REGISTRATION_MESSAGE;

            case "login_edit_button":
                userStates.put(chatId, "waiting_edit_login");
                String username = getUsernameByChatId(chatId);
                if (username == null) {
                    System.out.println("[Auth Command] Пользователь не найден в БД");
                    return "❌ **Пользователь не найден!**";
                }
                System.out.println("[Auth Command] Установлено состояние: waiting_edit_login для пользователя: " + username);
                return LOGIN_EDIT.replace("{username}", username);

            case "password_edit_button":
                userStates.put(chatId, "waiting_edit_password");
                System.out.println("[Auth Command] Установлено состояние: waiting_edit_password");
                return PASSWORD_EDIT;

            case "log_out_button":
                return LOG_OUT_CONFIRM;

            case "log_out_final_button":
                String user = getUsernameByChatId(chatId);
                if (user == null) {
                    System.out.println("[Auth Command] Пользователь не найден в БД для выхода");
                    return "❌ **Пользователь не найден!**";
                }
                System.out.println("[Auth Command] Выход пользователя из текущего чата: " + user);

                // Используем метод из AuthService чтобы отвязать чат
                boolean unlinked = authService.unlinkCurrentChat(chatId);
                if (unlinked) {
                    System.out.println("[Auth Command] Чат успешно отвязан");
                }

                // Очищаем состояние пользователя в этом чате
                clearUserState(chatId);
                return LOG_OUT_FINAL.replace("{username}", user);

            default:
                System.out.println("[Auth Command] Неизвестная команда: " + callbackData);
                return "❌ Неизвестная команда аутентификации";
        }
    }

    /**
     * Обрабатывает текстовые сообщения для аутентификации
     */
    public String handleTextMessage(String text, long chatId, boolean isTelegram) {
        System.out.println("[Auth Command] Обработка текста: " + text + " для chatId: " + chatId);

        String state = userStates.get(chatId);

        if (state == null) {
            System.out.println("[Auth Command] Состояние не установлено, возврат стартового сообщения");
            return START_MESSAGE;
        }

        System.out.println("[Auth Command] Текущее состояние: " + state);

        switch (state) {
            case "waiting_sing_in_login":
                userStates.put(chatId, "waiting_sing_in_password:" + text);
                System.out.println("[Auth Command] Установлено состояние: waiting_sing_in_password для логина: " + text);
                return SING_IN_LOGIN.replace("{username}", text);

            case "waiting_reg_login":
                userStates.put(chatId, "waiting_reg_password:" + text);
                System.out.println("[Auth Command] Установлено состояние: waiting_reg_password для логина: " + text);
                return REG_LOGIN.replace("{username}", text);

            case "waiting_edit_login":
                // Обработка изменения логина
                String currentUsername = getUsernameByChatId(chatId);
                if (currentUsername != null) {
                    System.out.println("[Auth Command] Смена логина: " + currentUsername + " -> " + text);
                    boolean success = authService.changeUsername(currentUsername, text);
                    if (success) {
                        // Обновляем привязку чата с новым логином
                        if (isTelegram) {
                            authService.linkTelegramChat(text, chatId);
                        } else {
                            authService.linkDiscordChannel(text, chatId);
                        }
                        userStates.remove(chatId);
                        System.out.println("[Auth Command] Логин успешно изменен");
                        return "✅ **Логин изменен!**\nНовый логин: " + text;
                    } else {
                        System.out.println("[Auth Command] Ошибка смены логина");
                        return "❌ **Ошибка изменения логина!**\nВозможно, такой логин уже занят";
                    }
                }
                userStates.remove(chatId);
                System.out.println("[Auth Command] Пользователь не найден для смены логина");
                return "❌ **Пользователь не найден!**";

            case "waiting_edit_password":
                // Обработка изменения пароля
                String usernameForPassword = getUsernameByChatId(chatId);
                if (usernameForPassword != null) {
                    System.out.println("[Auth Command] Смена пароля для пользователя: " + usernameForPassword);
                    boolean success = authService.resetPassword(usernameForPassword, text);
                    userStates.remove(chatId);
                    if (success) {
                        System.out.println("[Auth Command] Пароль успешно изменен");
                        return "✅ **Пароль изменен!**";
                    } else {
                        System.out.println("[Auth Command] Ошибка смены пароля");
                        return "❌ **Ошибка изменения пароля!**";
                    }
                }
                userStates.remove(chatId);
                System.out.println("[Auth Command] Пользователь не найден для смены пароля");
                return "❌ **Пользователь не найден!**";
        }

        // Обработка состояний с двоеточием (логин:пароль)
        if (state.startsWith("waiting_reg_password:") || state.startsWith("waiting_sing_in_password:")) {
            String[] passwordParts = state.split(":");
            if (passwordParts.length == 2) {
                String username = passwordParts[1];
                String password = text;

                if (state.startsWith("waiting_reg_password:")) {
                    System.out.println("[Auth Command] Регистрация пользователя: " + username);
                    boolean success = authService.registerUser(username, password);
                    if (success) {
                        if (isTelegram) {
                            authService.linkTelegramChat(username, chatId);
                        } else {
                            authService.linkDiscordChannel(username, chatId);
                        }
                        userStates.remove(chatId);
                        System.out.println("[Auth Command] Регистрация успешна");
                        return REG_FINAL.replace("{username}", username)
                                .replace("{password}", password);
                    } else {
                        userStates.put(chatId, "waiting_reg_login");
                        System.out.println("[Auth Command] Ошибка регистрации");
                        return "❌ **Ошибка регистрации!**\nЛогин уже занят или произошла ошибка";
                    }
                } else if (state.startsWith("waiting_sing_in_password:")) {
                    System.out.println("[Auth Command] Вход пользователя: " + username);
                    boolean authSuccess = authService.authenticate(username, password);
                    if (authSuccess) {
                        if (isTelegram) {
                            authService.linkTelegramChat(username, chatId);
                        } else {
                            authService.linkDiscordChannel(username, chatId);
                        }
                        userStates.remove(chatId);
                        System.out.println("[Auth Command] Вход успешен");
                        return SING_IN_SUCCESSFUL.replace("{username}", username);
                    } else {
                        userStates.put(chatId, "waiting_sing_in_login");
                        System.out.println("[Auth Command] Ошибка входа");
                        return SING_IN_ERROR;
                    }
                }
            }
        }

        System.out.println("[Auth Command] Неизвестное состояние: " + state);
        return "❌ Неизвестное состояние аутентификации";
    }

    /**
     * Очищает состояние пользователя
     */
    public void clearUserState(long chatId) {
        userStates.remove(chatId);
        System.out.println("[Auth Command] Состояние очищено для chatId: " + chatId);
    }
}