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
        System.out.println("[ScheduleTestHandler] Получен вопрос для chatId " + chatId + ": " + question.substring(0, Math.min(50, question.length())) + "...");
        return question;
    }

    /**
     * Обрабатывает ответ пользователя
     */
    public String handleAnswer(long chatId, String callbackData) {
        System.out.println("[ScheduleTestHandler] Обработка ответа для chatId: " + chatId + ", callbackData: " + callbackData);

        TestSession session = userSessions.get(chatId);
        if (session == null) {
            System.out.println("[ScheduleTestHandler] Ошибка: тест не активен для chatId: " + chatId);
            return "❌ Тест не активен.";
        }

        // Извлекаем букву ответа из callback_data (например, "B_button" -> "B")
        String answer = extractAnswerFromCallback(callbackData);
        System.out.println("[ScheduleTestHandler] Извлеченный ответ: " + answer);

        // Получаем данные текущего вопроса перед проверкой
        TestsData.QuestionData currentQuestion = session.getCurrentQuestionData();
        if (currentQuestion != null) {
            System.out.println("[ScheduleTestHandler] Текущий вопрос: " + currentQuestion.getEnglishWord() + " - " + currentQuestion.getTranslation());
            System.out.println("[ScheduleTestHandler] Правильный ответ: " + currentQuestion.getCorrectAnswer());
            System.out.println("[ScheduleTestHandler] Тип слова: " + currentQuestion.getWordType());
        }

        // Проверяем ответ
        boolean isCorrect = session.checkAnswer(answer);

        // Выводим информацию о правильности ответа и текущий счетчик
        int currentCorrect = session.getCorrectAnswersCount();
        int totalQuestions = session.getTotalQuestions();

        System.out.println("[ScheduleTestHandler] Ответ '" + answer + "' - " + (isCorrect ? "ПРАВИЛЬНЫЙ" : "НЕПРАВИЛЬНЫЙ"));
        System.out.println("[ScheduleTestHandler] Текущий счет: " + currentCorrect + "/" + totalQuestions + " правильных ответов");
        System.out.println("[ScheduleTestHandler] Прогресс: " + currentCorrect + " из " + totalQuestions + " (" +
                String.format("%.1f", (currentCorrect * 100.0 / totalQuestions)) + "%)");

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

        // Убираем суффикс "_button" и оставляем только первую букву
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
        System.out.println("[ScheduleTestHandler] Завершение теста для chatId: " + chatId);

        // Логируем распределение слов
        System.out.println("[ScheduleTestHandler] Приоритетные правильные слова: " + session.getPriorityCorrectWords());
        System.out.println("[ScheduleTestHandler] Приоритетные неправильные слова: " + session.getPriorityWrongWords());
        System.out.println("[ScheduleTestHandler] Новые правильные слова: " + session.getNewCorrectWords());
        System.out.println("[ScheduleTestHandler] Новые неправильные слова: " + session.getNewWrongWords());

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

        // Обновляем приоритеты для приоритетных слов с правильными ответами
        for (int i = 0; i < session.getPriorityCorrectWords().size(); i++) {
            String word = session.getPriorityCorrectWords().get(i);
            String translation = session.getPriorityCorrectTranslations().get(i);
            System.out.println("[ScheduleTestHandler] Обновление приоритета (правильно, приоритетное): " + word);
            scheduleTests.updateWordPriority(userId, word, translation, true, true);
        }

        // Обновляем приоритеты для приоритетных слов с неправильными ответами
        for (int i = 0; i < session.getPriorityWrongWords().size(); i++) {
            String word = session.getPriorityWrongWords().get(i);
            String translation = session.getPriorityWrongTranslations().get(i);
            System.out.println("[ScheduleTestHandler] Обновление приоритета (неправильно, приоритетное): " + word);
            scheduleTests.updateWordPriority(userId, word, translation, false, true);
        }

        // Обновляем приоритеты для новых слов с правильными ответами
        for (int i = 0; i < session.getNewCorrectWords().size(); i++) {
            String word = session.getNewCorrectWords().get(i);
            String translation = session.getNewCorrectTranslations().get(i);
            System.out.println("[ScheduleTestHandler] Обновление приоритета (правильно, новое): " + word);
            scheduleTests.updateWordPriority(userId, word, translation, true, false);
        }

        // Обновляем приоритеты для новых слов с неправильными ответами
        for (int i = 0; i < session.getNewWrongWords().size(); i++) {
            String word = session.getNewWrongWords().get(i);
            String translation = session.getNewWrongTranslations().get(i);
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
     * Получает данные текущего вопроса
     */
    public TestsData.QuestionData getCurrentQuestionData(long chatId) {
        TestSession session = userSessions.get(chatId);
        TestsData.QuestionData questionData = session != null ? session.getCurrentQuestionData() : null;
        if (questionData != null) {
            System.out.println("[ScheduleTestHandler] Получены данные вопроса: " + questionData.getEnglishWord() + " - " + questionData.getTranslation());
        } else {
            System.out.println("[ScheduleTestHandler] Данные вопроса не найдены для chatId: " + chatId);
        }
        return questionData;
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

        // Добавляем информацию об изменении приоритетов
        sb.append("📈 Изменения приоритетов:\n");

        if (!session.getPriorityCorrectWords().isEmpty()) {
            sb.append("• Приоритетные слова, которые вы знаете: ").append(session.getPriorityCorrectWords().size()).append("\n");
        }
        if (!session.getPriorityWrongWords().isEmpty()) {
            sb.append("• Приоритетные слова для повторения: ").append(session.getPriorityWrongWords().size()).append("\n");
        }
        if (!session.getNewCorrectWords().isEmpty()) {
            sb.append("• Новые слова, которые вы усвоили: ").append(session.getNewCorrectWords().size()).append("\n");
        }
        if (!session.getNewWrongWords().isEmpty()) {
            sb.append("• Новые слова для изучения: ").append(session.getNewWrongWords().size()).append("\n");
        }

        sb.append("\n");

        if (percentage >= 80) {
            sb.append("🏆 Отличный результат! Вы хорошо знаете слова!");
        } else if (percentage >= 60) {
            sb.append("👍 Хороший результат! Продолжайте практиковаться!");
        } else {
            sb.append("💪 Есть над чем поработать! Учите слова регулярно!");
        }

        return sb.toString();
    }
}