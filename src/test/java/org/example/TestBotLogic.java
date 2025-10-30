package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * TestBotLogic - тестирует обработку текстовых команд бота.
 * Проверяет правильность ответов на команды /start, /help и неизвестные команды.
 */

public class TestBotLogic {

    private final BotLogic botLogic = new BotLogic();

    /**
     * Проверка команды /start
     */
    @Test
    void testStartCommand() {
        String result = botLogic.handleCommand("/start");

        Assertions.assertEquals("🌍 *Добро пожаловать в GlobeTalk!* 🌍\n\n" +
                "Ваш персональный помощник в изучении иностранных языков! 🎯\n\n" +
                "📚 **Перед началом обучения** рекомендую пройти короткий тест для определения вашего текущего уровня владения языком.\n\n" +
                "💡 Это поможет нам подобрать оптимальную программу обучения именно для вас!\n\n" +
                "🔍 Для просмотра всех команд нажмите /help\n\n" +
                "🚀 **Вы готовы начать тест?**", result);
    }

    /**
     * Проверка команды /help
     */
    @Test
    void testHelpCommand() {
        String result = botLogic.handleCommand("/help");

        Assertions.assertEquals("🌍 *GlobeTalk - Изучение иностранных языков* 🌍\n\n" +

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

                "🚀 **Начните с команды /start чтобы определить ваш уровень!**", result);
    }

    /**
     * Проверка неизвестной команды
     */
    @Test
    void testUnknownCommand() {
        String result = botLogic.handleCommand("/unknown");

        Assertions.assertEquals("Неизвестная команда. Введите /help для списка доступных команд.", result);
    }
}
