package org.example.ScheduledOldWord;

import org.example.Dictionary.Word;
import org.example.Dictionary.DictionaryService;
import org.example.Dictionary.DictionaryServiceImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для работы со словами с низким приоритетом
 * Находит одно слово с минимальным приоритетом и генерирует по нему тест
 */
public class OldWord {
    private final DictionaryService dictionaryService;
    private final OldWordGenerator testGenerator;
    private final OldWordParser testParser;

    // Храним активные тесты для пользователей (по chatId)
    private final ConcurrentHashMap<Long, OldWordData> activeTests = new ConcurrentHashMap<>();

    public OldWord() {
        this.dictionaryService = new DictionaryServiceImpl();
        this.testGenerator = new OldWordGenerator(this);
        this.testParser = new OldWordParser();
    }

    /**
     * Проверяет, активен ли тест для пользователя
     */
    public boolean isTestActive(long chatId) {
        return activeTests.containsKey(chatId);
    }

    /**
     * Получает ID пользователя по chatId
     */
    private long getUserId(long chatId) throws SQLException {
        return dictionaryService.getUserIdByChatId(chatId);
    }

    /**
     * Находит ОДНО слово с самым низким приоритетом для пользователя
     */
    private Word getWordWithLowestPriority(long userId) {
        try {
            List<Word> allWords = dictionaryService.getAllWords(userId);

            if (allWords.isEmpty()) {
                System.out.println("[OldWord] У пользователя " + userId + " нет слов в словаре");
                return null;
            }

            // Находим минимальный приоритет
            int minPriority = 11;
            for (Word word : allWords) {
                if (word.getPriority() < minPriority) {
                    minPriority = word.getPriority();
                }
            }

            // Собираем все слова с минимальным приоритетом
            List<Word> lowestPriorityWords = new ArrayList<>();
            for (Word word : allWords) {
                if (word.getPriority() == minPriority) {
                    lowestPriorityWords.add(word);
                }
            }

            if (lowestPriorityWords.isEmpty()) {
                System.out.println("[OldWord] Не найдено слов с минимальным приоритетом");
                return null;
            }

            // Выбираем случайное слово из слов с минимальным приоритетом
            Random random = new Random();
            Word selectedWord = lowestPriorityWords.get(random.nextInt(lowestPriorityWords.size()));

            System.out.println("[OldWord] Найдено слово с низким приоритетом: '" +
                    selectedWord.getEnglishWord() + "' - '" +
                    selectedWord.getTranslation() + "' (приоритет: " +
                    selectedWord.getPriority() + ") из " + lowestPriorityWords.size() + " вариантов");

            return selectedWord;

        } catch (SQLException e) {
            System.err.println("[OldWord] Ошибка получения слов: " + e.getMessage());
            return null;
        }
    }

    /**
     * Запускает процесс тестирования слова с низким приоритетом
     * Возвращает отформатированный текст теста для пользователя
     */
    public String startLowPriorityTest(long chatId) {
        try {
            long userId = getUserId(chatId);
            Word word = getWordWithLowestPriority(userId);
            if (word == null) {
                throw new RuntimeException(" [OldWord] У вас пока нет слов для изучения. Добавьте слова в словарь!");
            }

            // Генерируем тест
            String testText = testGenerator.generateTest(word.getEnglishWord(), word.getTranslation());

            System.out.println("\n    [OldWord] Сгенерированный текст:");
            System.out.println(testText);

            if (testText.contains("ошибка") || testText.contains("API ключ")) {
                throw new RuntimeException("[OldWord]  Не удалось сгенерировать тест. Попробуйте позже.");
            }

            // Парсим тест и передаем полную информацию о слове
            OldWordData parsedData = testParser.parseTest(testText, word.getEnglishWord(), word.getTranslation());

            // Сохраняем ID слова из базы данных для последующего обновления
            parsedData.setWordId(word.getId());
            parsedData.setCurrentPriority(word.getPriority());

            // Сохраняем активный тест
            activeTests.put(chatId, parsedData);

            // Форматируем для показа пользователю
            return formatTestForDisplay(parsedData);

        } catch (SQLException e) {
            throw new RuntimeException("[OldWord]  Ошибка доступа к словарю: " + e.getMessage());
        }
    }

    /**
     * Форматирует ParsedTestData в красивый вывод для пользователя
     */
    public String formatTestForDisplay(OldWordData testData) {
        String header =
                "📚 *Кажется найдено забытое слово из словаря*\n" +
                        "Необходимо срочно освежить в памяти его значение 💫\n\n";

        return header + testData.getFullQuestion();
    }

    /**
     * Проверяет ответ пользователя
     */
    public boolean checkUserAnswer(OldWordData testData, String userAnswer) {
        String normalizedUserAnswer = userAnswer.trim().toUpperCase();
        String correctAnswer = testData.getCorrectAnswer();

        return normalizedUserAnswer.equals(correctAnswer);
    }

    /**
     * Обрабатывает ответ пользователя и обновляет приоритет
     * Использует данные из активного теста
     */
    public String handleUserAnswer(long chatId, String userAnswer) {
        try {
            OldWordData testData = activeTests.get(chatId);
            if (testData == null) {
                return "❌ Активный тест не найден. Начните тест заново.";
            }

            long userId = getUserId(chatId);

            // Проверяем ответ
            boolean isCorrect = checkUserAnswer(testData, userAnswer);

            // Обрабатываем ответ
            String result = handleAnswer(userId, testData, isCorrect, testData.getCorrectAnswer());

            // Очищаем активный тест
            activeTests.remove(chatId);

            return result;

        } catch (Exception e) {
            System.err.println("[OldWord] Ошибка обработки ответа: " + e.getMessage());
            activeTests.remove(chatId);
            return "❌ Ошибка при обработке ответа";
        }
    }

    /**
     * Обрабатывает ответ пользователя и обновляет приоритет
     */
    private String handleAnswer(long userId, OldWordData testData, boolean isCorrect, String correctAnswer) {
        try {
            String englishWord = testData.getEnglishWord();
            String translation = testData.getTranslation();
            int wordId = testData.getWordId();
            int currentPriority = testData.getCurrentPriority();

            int newPriority;

            // Если ответ правильный - понижаем приоритет, если неправильный - повышаем
            if (isCorrect) {
                newPriority = Math.max(0, currentPriority - 1);
            } else {
                newPriority = Math.min(10, currentPriority + 1);
            }

            // Обновляем приоритет в базе данных
            dictionaryService.updateWordPriority(userId, wordId, newPriority);

            // Формируем отчет
            String report = formatPriorityReport(englishWord, translation, isCorrect, correctAnswer);
            System.out.println("[OldWord] Приоритет слова '" + englishWord + "' изменен: " +
                    currentPriority + " -> " + newPriority + " (правильно: " + isCorrect + ")");

            return report;

        } catch (SQLException e) {
            System.err.println("[OldWord] Ошибка обновления приоритета: " + e.getMessage());
            return "❌ Ошибка при обновлении приоритета слова";
        }
    }

    /**
     * Форматирует отчет об изменении приоритета
     */
    private String formatPriorityReport(String englishWord, String translation, boolean isCorrect, String correctAnswer) {
        if (isCorrect) {
            return "✅ Ваш ответ верный! 😎\n" +
                    "Ничего себе вот это память! 🧠🧠🧠\n\n" +
                    "Приоритет слова \"" + englishWord + "\" понижен.";
        } else {
            return String.format(
                    "❌ Почти угадали! 😊\n" +
                            "Правильный ответ: %s\n\n" +
                            "📝 Напоминаем перевод слова:\n" +
                            "• 🔤 Слово: %s\n" +
                            "• 🌐 Перевод: %s\n\n" +
                            "Теперь это слово будет попадаться чаще!",
                    correctAnswer,
                    englishWord,
                    translation
            );
        }
    }

    /**
     * Очищает активный тест для пользователя
     */
    public void clearActiveTest(long chatId) {
        OldWordData removed = activeTests.remove(chatId);
        if (removed != null) {
            System.out.println("[OldWord] Активный тест очищен для chatId: " + chatId);
        }
    }
}