package org.example.ScheduledTests;

import java.util.HashMap;
import java.util.Map;

/**
 * Обработчик теста для отложенных тестов
 * Управляет процессом прохождения теста пользователем
 */
public class ScheduleTestHandler {

    // Храним состояние теста для каждого пользователя
    private final Map<Long, TestSession> userSessions = new HashMap<>();
    private final ScheduleTests scheduleTests;

    public ScheduleTestHandler(ScheduleTests scheduleTests) {
        this.scheduleTests = scheduleTests;
        System.out.println("[ScheduleTestHandler] Инициализирован");
    }

    /**
     * Начинает новый тест для пользователя
     */
    public String startTest(long chatId, TestsData testsData, long userId) {
        System.out.println("[ScheduleTestHandler] Начало теста для chatId: " + chatId + ", userId: " + userId);

        if (testsData.getQuestions().isEmpty()) {
            System.out.println("[ScheduleTestHandler] Ошибка: нет вопросов для теста");
            return "❌ Не удалось загрузить вопросы для теста.";
        }

        // Создаем сессию теста
        TestSession session = new TestSession(testsData, userId);
        userSessions.put(chatId, session);

        String firstQuestion = getCurrentQuestion(chatId);
        System.out.println("[ScheduleTestHandler] Первый вопрос отправлен: " + firstQuestion.substring(0, Math.min(50, firstQuestion.length())) + "...");
        return firstQuestion;
    }

    /**
     * Получает текущий вопрос для пользователя
     */
    public String getCurrentQuestion(long chatId) {
        TestSession session = userSessions.get(chatId);
        if (session == null) {
            System.out.println("[ScheduleTestHandler] Ошибка: тест не начат для chatId: " + chatId);
            return "❌ Тест не начат. Используйте команду для начала теста.";
        }

        String question = session.getCurrentQuestion();
        return question;
    }

    /**
     * Обрабатывает ответ пользователя
     */
    public String handleAnswer(long chatId, String callbackData) {
        System.out.println("[ScheduleTestHandler] Обработка ответа для chatId: " + chatId);

        TestSession session = userSessions.get(chatId);
        if (session == null) {
            System.out.println("[ScheduleTestHandler] Ошибка: тест не активен для chatId: " + chatId);
            return "❌ Тест не активен.";
        }

        // Извлекаем букву ответа из callback_data (например, "B_button" -> "B")
        String answer = extractAnswerFromCallback(callbackData);

        boolean isCorrect = session.checkAnswer(answer);
        System.out.println("[ScheduleTestHandler] Ответ " + answer + " - " + (isCorrect ? "ПРАВИЛЬНЫЙ" : "НЕПРАВИЛЬНЫЙ"));
        System.out.println("[ScheduleTestHandler] Текущий счет: " + session.getCorrectAnswersCount() + "/" + session.getTotalQuestions() + " правильных ответов");

        // Переходим к следующему вопросу
        session.nextQuestion();

        if (session.isTestCompleted()) {
            // Тест завершен - обновляем приоритеты и возвращаем результат
            System.out.println("[ScheduleTestHandler] Тест завершен для chatId: " + chatId);
            String result = completeTest(chatId, session);
            userSessions.remove(chatId);
            return result;
        } else {
            String nextQuestion = getCurrentQuestion(chatId);
            System.out.println("[ScheduleTestHandler] Следующий вопрос отправлен");
            return nextQuestion;
        }
    }

    /**
     * Извлекает букву ответа из callback_data
     * Пример: "A_button" -> "A", "B_button" -> "B"
     */
    private String extractAnswerFromCallback(String callbackData) {
        if (callbackData == null || callbackData.isEmpty()) {
            return "";
        }

        // Убираем "_button" и оставляем только первую букву
        if (callbackData.endsWith("_button")) {
            return callbackData.substring(0, 1);
        }

        // Если формат неожиданный, возвращаем как есть (на всякий случай)
        return callbackData;
    }

    /**
     * Завершает тест и обновляет приоритеты слов
     */
    private String completeTest(long chatId, TestSession session) {
        // Обновляем приоритеты для всех слов
        updateWordPriorities(session);

        // Форматируем результат
        String result = formatTestResult(session);
        System.out.println("[ScheduleTestHandler] Результат теста сформирован");
        return result;
    }

    /**
     * Обновляет приоритеты слов в базе данных
     */
    private void updateWordPriorities(TestSession session) {
        long userId = session.getUserId();
        System.out.println("[ScheduleTestHandler] Обновление приоритетов для userId: " + userId);

        // ДОБАВЛЕНА ПРОВЕРКА ДАННЫХ
        System.out.println("\n\n        [ScheduleTestHandler] ПРОВЕРКА ДАННЫХ ПЕРЕД ОБНОВЛЕНИЕМ");
        System.out.println("Приоритетные правильные слова: " + session.getPriorityCorrectWords());
        System.out.println("Приоритетные неправильные слова: " + session.getPriorityWrongWords());
        System.out.println("Новые правильные слова: " + session.getNewCorrectWords());
        System.out.println("Новые неправильные слова: " + session.getNewWrongWords());

        // Обновляем приоритеты для приоритетных слов с правильными ответами
        for (int i = 0; i < session.getPriorityCorrectWords().size(); i++) {
            String word = session.getPriorityCorrectWords().get(i);
            String translation = session.getPriorityCorrectTranslations().get(i);

            // ПРОВЕРКА НА ПУСТЫЕ ЗНАЧЕНИЯ
            if (word == null || word.trim().isEmpty() || translation == null || translation.trim().isEmpty()) {
                System.err.println("❌ ПРОПУСК: Пустое слово или перевод в приоритетных правильных, индекс " + i);
                continue;
            }

            System.out.println("[ScheduleTestHandler] Обновление приоритета (правильно, приоритетное): " + word);
            scheduleTests.updateWordPriority(userId, word, translation, true, true);
        }

        // Обновляем приоритеты для приоритетных слов с неправильными ответами
        for (int i = 0; i < session.getPriorityWrongWords().size(); i++) {
            String word = session.getPriorityWrongWords().get(i);
            String translation = session.getPriorityWrongTranslations().get(i);

            // ПРОВЕРКА НА ПУСТЫЕ ЗНАЧЕНИЯ
            if (word == null || word.trim().isEmpty() || translation == null || translation.trim().isEmpty()) {
                System.err.println("❌ ПРОПУСК: Пустое слово или перевод в приоритетных неправильных, индекс " + i);
                continue;
            }

            System.out.println("[ScheduleTestHandler] Обновление приоритета (неправильно, приоритетное): " + word);
            scheduleTests.updateWordPriority(userId, word, translation, false, true);
        }

        // Обновляем приоритеты для новых слов с правильными ответами
        for (int i = 0; i < session.getNewCorrectWords().size(); i++) {
            String word = session.getNewCorrectWords().get(i);
            String translation = session.getNewCorrectTranslations().get(i);

            // ПРОВЕРКА НА ПУСТЫЕ ЗНАЧЕНИЯ
            if (word == null || word.trim().isEmpty() || translation == null || translation.trim().isEmpty()) {
                System.err.println("❌ ПРОПУСК: Пустое слово или перевод в новых правильных, индекс " + i);
                continue;
            }

            System.out.println("[ScheduleTestHandler] Обновление приоритета (правильно, новое): " + word);
            scheduleTests.updateWordPriority(userId, word, translation, true, false);
        }

        // Обновляем приоритеты для новых слов с неправильными ответами
        for (int i = 0; i < session.getNewWrongWords().size(); i++) {
            String word = session.getNewWrongWords().get(i);
            String translation = session.getNewWrongTranslations().get(i);

            // ПРОВЕРКА НА ПУСТЫЕ ЗНАЧЕНИЯ
            if (word == null || word.trim().isEmpty() || translation == null || translation.trim().isEmpty()) {
                System.err.println("❌ ПРОПУСК: Пустое слово или перевод в новых неправильных, индекс " + i);
                continue;
            }

            System.out.println("[ScheduleTestHandler] Обновление приоритета (неправильно, новое): " + word);
            scheduleTests.updateWordPriority(userId, word, translation, false, false);
        }

        System.out.println("[ScheduleTestHandler] Все приоритеты обновлены");
    }

    /**
     * Проверяет, активен ли тест для пользователя
     */
    public boolean isTestActive(long chatId) {
        TestSession session = userSessions.get(chatId);
        boolean isActive = session != null && !session.isTestCompleted();
        System.out.println("[ScheduleTestHandler] Проверка активности теста для chatId " + chatId + ": " + isActive);
        return isActive;
    }
    /**
     * Форматирует результат теста
     */
    private String formatTestResult(TestSession session) {
        int total = session.getTotalQuestions();
        int correct = session.getCorrectAnswersCount();
        int percentage = (int) ((correct * 100.0) / total);

        System.out.println("[ScheduleTestHandler] Формирование результата: " + correct + "/" + total + " (" + percentage + "%)");

        StringBuilder sb = new StringBuilder();
        sb.append("🎉 Тест завершен! 🎉\n\n");
        sb.append("📊 Результаты:\n");
        sb.append("• Всего вопросов: ").append(total).append("\n");
        sb.append("• Правильных ответов: ").append(correct).append("\n");
        sb.append("• Ошибок: ").append(total - correct).append("\n");
        sb.append("• Процент правильных: ").append(percentage).append("%\n\n");

        // Добавляем мотивационную фразу в зависимости от результата
        if (percentage >= 80) {
            sb.append("🎉 *Блестящий результат!*\n");
            sb.append("Вы ответили правильно на ").append(correct).append(" из ").append(total).append(" вопросов!\n");
            sb.append("Это уровень уверенного знатока языка — так держать! 🚀\n\n");
        } else if (percentage >= 50) {
            sb.append("📖 *Хорошая основа для роста!*\n");
            sb.append("Ваш результат: ").append(correct).append(" из ").append(total).append(" правильных ответов.\n");
            sb.append("Вы уже многое знаете, а пробелы — это возможности для новых открытий!\n\n");
        } else {
            sb.append("🌱 *Начало пути!*\n");
            sb.append("Вы ответили правильно на ").append(correct).append(" из ").append(total).append(" вопросов.\n");
            sb.append("Каждый эксперт когда-то начинал с первого шага — и вы его уже сделали!\n\n");
        }

        // Добавляем информацию об изменении приоритетов
        sb.append("📈 Изменения приоритетов:\n");

        if (!session.getPriorityCorrectWords().isEmpty()) {
            sb.append("• Слова, которые вы хорошо знаете: ").append(session.getPriorityCorrectWords().size()).append("\n");
        }
        if (!session.getPriorityWrongWords().isEmpty()) {
            sb.append("• Слова для повторения: ").append(session.getPriorityWrongWords().size()).append("\n");
        }
        if (!session.getNewCorrectWords().isEmpty()) {
            sb.append("• Новые слова, которые вы знаете: ").append(session.getNewCorrectWords().size()).append("\n");
        }
        if (!session.getNewWrongWords().isEmpty()) {
            sb.append("• Новые слова для изучения: ").append(session.getNewWrongWords().size()).append("\n");
        }

        return sb.toString();
    }
}