package org.example.ScheduledTests;

import org.example.Dictionary.DictionaryService;
import org.example.Dictionary.DictionaryServiceImpl;
import org.example.Dictionary.Word;

import java.sql.SQLException;
import java.util.*;

/**
 * ScheduleTests - класс для работы с отложенными тестами на повторение слов
 */
public class ScheduleTests {
    private final DictionaryService dictionaryService;
    private final ScheduleGenerateTests scheduleGenerateTests;
    private final TestsParser testsParser;
    private final ScheduleTestHandler scheduleTestHandler;

    public ScheduleTests() {
        this.dictionaryService = new DictionaryServiceImpl();
        this.scheduleGenerateTests = new ScheduleGenerateTests(this);
        this.testsParser = new TestsParser();
        this.scheduleTestHandler = new ScheduleTestHandler(this);
    }

    /**
     * Обрабатывает нажатия кнопок для отложенных тестов
     */
    public String handleButtonClick(String callbackData, long chatId) {
        System.out.println("[ScheduleTests] Обработка кнопки: " + callbackData + " для chatId: " + chatId);

        switch (callbackData) {
            case "yes_schedule_test_button":
                return startScheduleTest(chatId);

            case "no_schedule_test_button":
                return getTestDeclineMessage();

            default:
                return "❌ Неизвестная команда теста";
        }
    }

    /**
     * Запускает отложенный тест для пользователя
     */
    public String startScheduleTest(long chatId) {
        try {
            long userId = getUserId(chatId);
            System.out.println("[ScheduleTests] Запуск отложенного теста для userId: " + userId);

            // Генерируем тест
            String testText = scheduleGenerateTests.generateTest(userId);

            if (testText.contains("ошибка") || testText.contains("API ключ")) {
                return "❌ Не удалось сгенерировать тест. Попробуйте позже.";
            }

            // Парсим тест
            TestsData testsData = testsParser.parseTest(testText);

            for (int i = 0; i < testsData.getQuestions().size(); i++) {
                TestsData.QuestionData q = testsData.getQuestions().get(i);
            }

            if (testsData.getQuestions().isEmpty()) {
                return "❌ Не удалось распознать вопросы теста.";
            }

            // Запускаем тест через handler
            return scheduleTestHandler.startTest(chatId, testsData, userId);

        } catch (Exception e) {
            System.err.println("[ScheduleTests] Ошибка запуска отложенного теста: " + e.getMessage());
            return "❌ Ошибка при запуске теста.";
        }
    }

    /**
     * Обрабатывает ответы пользователя в активном тесте
     */
    public String handleTestAnswer(String callbackData, long chatId) {
        if (!scheduleTestHandler.isTestActive(chatId)) {
            return "❌ Тест не активен. Начните заново.";
        }

        return scheduleTestHandler.handleAnswer(chatId, callbackData);
    }

    /**
     * Проверяет, активен ли тест для пользователя
     */
    public boolean isTestActive(long chatId) {
        return scheduleTestHandler.isTestActive(chatId);
    }

    /**
     * Возвращает приветственное сообщение для начала теста
     */
    public String getTestIntroduction() {
        return "🌙 Момент истины настал!\n\n" +
                "Знания, которые вы собирали по крупицам в течении недели и не только, готовы проверке!\n\n" +
                "✨ Готовы бросить вызов себе?";
    }

    /**
     * Возвращает сообщение об отказе от теста
     */
    public String getTestDeclineMessage() {
        return "Хорошо, не сейчас ✨\n\n" +
                "Знания никуда не убегут — они терпеливо ждут своего часа.\n\n" +
                "Когда почувствуете готовность, просто нажмите кнопку /scheduled_test в боковом меню - и мы продолжим!\n\n" +
                "🌟 Ваше обучение — в ваших руках";
    }

    /**
     * Возвращает приглашение на отложенный тест с кнопками
     */
    public String getScheduleTestInvitation() {
        return getTestIntroduction();
    }

    /**
     * Получает ID пользователя по chatId
     */
    private long getUserId(long chatId) throws SQLException {
        return dictionaryService.getUserIdByChatId(chatId);
    }

    /**
     * Ищет до 6 английских слов пользователя с максимальным приоритетом
     * и возвращает попарно слово на английском и перевод
     * Если слов недостаточно - возвращает все имеющиеся слова
     */
    public List<String[]> getWordsWithMaxPriority(long userId) {
        try {
            List<Word> allWords = dictionaryService.getAllWords(userId);

            if (allWords.isEmpty()) {
                System.out.println("[ScheduleTests] У пользователя " + userId + " нет слов в словаре");
                return Collections.emptyList();
            }

            // Если слов меньше или равно 6, возвращаем все что есть
            if (allWords.size() <= 6) {
                Collections.sort(allWords, (w1, w2) -> w2.getPriority() - w1.getPriority());
                System.out.println("[ScheduleTests] Найдено " + allWords.size() +
                        " слов (все доступные) для пользователя " + userId);
                return convertToPairs(allWords);
            }

            // Создаем список для топ-6 слов
            List<Word> topWords = new ArrayList<>(6);

            for (Word word : allWords) {
                if (topWords.size() < 6) {
                    topWords.add(word);
                    Collections.sort(topWords, (w1, w2) -> w2.getPriority() - w1.getPriority());
                } else {
                    if (word.getPriority() > topWords.get(5).getPriority()) {
                        topWords.set(5, word);
                        Collections.sort(topWords, (w1, w2) -> w2.getPriority() - w1.getPriority());
                    }
                }
            }

            return convertToPairs(topWords);

        } catch (SQLException e) {
            System.err.println("[ScheduleTests] Ошибка получения слов для теста: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Преобразует список слов в список пар [английское слово, перевод]
     */
    private List<String[]> convertToPairs(List<Word> words) {
        List<String[]> pairs = new ArrayList<>();
        for (Word word : words) {
            pairs.add(new String[]{
                    word.getEnglishWord(),
                    word.getTranslation()
            });
        }
        return pairs;
    }

    /**
     * Обновляет приоритет слова в базе данных
     */
    public void updateWordPriority(long userId, String englishWord, String translation, boolean isCorrect, boolean isPriorityWord) {
        try {
            Word word = dictionaryService.getWordByEnglish(userId, englishWord);

            // Если слово УЖЕ ЕСТЬ в словаре - применяем старую логику
            if (word != null) {
                int currentPriority = word.getPriority();
                int newPriority;

                // Если ответил неправильно - приоритет повышается, если правильно - уменьшается
                newPriority = isCorrect ? Math.max(0, currentPriority - 1) : Math.min(10, currentPriority + 1);

                dictionaryService.updateWordPriority(userId, word.getId(), newPriority);

                // ОТЧЕТ ОБ ОБНОВЛЕНИИ ПРИОРИТЕТА
                System.out.println("\n\n        ОТЧЕТ ОБ ОБНОВЛЕНИИ ПРИОРИТЕТА");
                System.out.println("- Слово: '" + englishWord + "'");
                System.out.println("- Перевод: '" + translation + "'");
                System.out.println("- Старый приоритет: " + currentPriority);
                System.out.println("- Новый приоритет: " + newPriority);
                System.out.println("- Изменение: " +
                        (newPriority > currentPriority ? "УВЕЛИЧЕН на " + (newPriority - currentPriority) :
                                newPriority < currentPriority ? "УМЕНЬШЕН на " + (currentPriority - newPriority) :
                                        "→ БЕЗ ИЗМЕНЕНИЙ"));
                System.out.println("- Ответ: " + (isCorrect ? "ПРАВИЛЬНЫЙ" : "НЕПРАВИЛЬНЫЙ"));
                System.out.println("- Тип: " + (isPriorityWord ? "ПРИОРИТЕТНОЕ" : "НОВОЕ"));
                return;
            }

            // Если слово НОВОЕ (нет в словаре) - добавляем ТОЛЬКО если пользователь ответил ПРАВИЛЬНО
            if (!isCorrect) {
                System.out.println("[ScheduleTests] Новое слово '" + englishWord + "' не добавлено в словарь (неправильный ответ)");
                return;
            }

            // Добавляем новое слово в словарь ТОЛЬКО если пользователь ответил ПРАВИЛЬНО
            System.out.println("[ScheduleTests] Слово не найдено: " + englishWord + ", добавляем как новое");

            int initialPriority = 3; // Всегда 3 для новых слов, которые пользователь знает
            dictionaryService.addWord(userId, englishWord, translation, initialPriority);

            // ОТЧЕТ О ДОБАВЛЕНИИ НОВОГО СЛОВА
            System.out.println("\n\n        ОТЧЕТ О ДОБАВЛЕНИИ НОВОГО СЛОВА");
            System.out.println("- Слово: '" + englishWord + "'");
            System.out.println("- Перевод: '" + translation + "'");
            System.out.println("- Начальный приоритет: " + initialPriority);
            System.out.println("- Ответ: ПРАВИЛЬНЫЙ");
            System.out.println("-️ Тип: " + (isPriorityWord ? "ПРИОРИТЕТНОЕ" : "НОВОЕ"));


        } catch (SQLException e) {
            System.err.println("[ScheduleTests] ❌ Ошибка обновления приоритета слова '" + englishWord + "': " + e.getMessage());
        }
    }
}