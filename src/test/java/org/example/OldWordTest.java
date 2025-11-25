package org.example;

import org.example.ScheduledOldWord.OldWord;
import org.example.ScheduledOldWord.OldWordData;
import org.example.Dictionary.DictionaryService;
import org.example.Dictionary.Word;
import org.example.ScheduledOldWord.OldWordParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.util.*;

/**
 * Комплексные тесты для класса OldWord с мокированием DictionaryService
 * Работает без интернета - использует ручное создание тестов
 */
public class OldWordTest {

    private OldWord oldWord;
    private MockDictionaryService mockDictionaryService;
    private final long AUTHORIZED_USER_ID = 12345L;
    private final long AUTHORIZED_CHAT_ID = 67890L;
    private final long UNAUTHORIZED_CHAT_ID = 111222333L;

    /**
     * Мок-реализация DictionaryService для тестирования OldWord
     */
    private class MockDictionaryService implements DictionaryService {
        private final Map<Long, List<Word>> userWords = new HashMap<>();
        private int nextWordId = 1;

        public void addTestWords(long userId, List<Word> words) {
            userWords.put(userId, new ArrayList<>(words));
        }

        public void clearUserWords(long userId) {
            userWords.remove(userId);
        }

        @Override
        public long getUserIdByChatId(long chatId) throws SQLException {
            if (chatId == AUTHORIZED_CHAT_ID) {
                return AUTHORIZED_USER_ID;
            }
            throw new SQLException("Пользователь не авторизован для chatId: " + chatId);
        }

        @Override
        public List<Word> getAllWords(long userId) throws SQLException {
            return Collections.unmodifiableList(userWords.getOrDefault(userId, Collections.emptyList()));
        }

        @Override
        public Word getWordByEnglish(long userId, String englishWord) throws SQLException {
            return userWords.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(word -> word.getEnglishWord().equalsIgnoreCase(englishWord))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void addWord(long userId, String englishWord, String translation, int priority) throws SQLException {
            List<Word> words = userWords.computeIfAbsent(userId, k -> new ArrayList<>());

            if (words.stream().anyMatch(word -> word.getEnglishWord().equalsIgnoreCase(englishWord))) {
                throw new SQLException("Слово уже существует в словаре: " + englishWord);
            }

            Word newWord = new Word(nextWordId++, userId, englishWord, translation, priority);
            words.add(newWord);
        }

        @Override
        public void updateWord(long userId, int wordId, String newEnglishWord, String newTranslation, Integer newPriority) throws SQLException {
            List<Word> words = userWords.get(userId);
            if (words == null) {
                throw new SQLException("Словарь пользователя не найден: " + userId);
            }

            for (int i = 0; i < words.size(); i++) {
                Word word = words.get(i);
                if (word.getId() == wordId) {
                    Word updatedWord = new Word(wordId, userId, newEnglishWord, newTranslation,
                            newPriority != null ? newPriority : word.getPriority());
                    words.set(i, updatedWord);
                    return;
                }
            }
            throw new SQLException("Слово не найдено: " + wordId);
        }

        @Override
        public void updateWordPriority(long userId, int wordId, int newPriority) throws SQLException {
            List<Word> words = userWords.get(userId);
            if (words == null) {
                throw new SQLException("Словарь пользователя не найден: " + userId);
            }

            for (int i = 0; i < words.size(); i++) {
                Word word = words.get(i);
                if (word.getId() == wordId) {
                    Word updatedWord = new Word(wordId, userId, word.getEnglishWord(),
                            word.getTranslation(), newPriority);
                    words.set(i, updatedWord);
                    return;
                }
            }
            throw new SQLException("Слово не найдено: " + wordId);
        }

        @Override
        public void deleteWord(long userId, int wordId) throws SQLException {
            List<Word> words = userWords.get(userId);
            if (words == null) {
                throw new SQLException("Словарь пользователя не найден: " + userId);
            }

            boolean removed = words.removeIf(word -> word.getId() == wordId);
            if (!removed) {
                throw new SQLException("Слово не найдено: " + wordId);
            }
        }

        @Override
        public Word getWordById(long userId, int wordId) throws SQLException {
            return userWords.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(word -> word.getId() == wordId)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Word> getWordsByPriority(long userId, int priority) throws SQLException {
            return userWords.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(word -> word.getPriority() == priority)
                    .toList();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockDictionaryService = new MockDictionaryService();
        // Используем конструктор с инъекцией зависимостей
        oldWord = new OldWord(mockDictionaryService);
        setupTestWords();
    }

    /**
     * Настройка тестовых данных
     */
    private void setupTestWords() {
        List<Word> testWords = Arrays.asList(
                new Word(1, AUTHORIZED_USER_ID, "accomplishment", "достижение", 3),
                new Word(2, AUTHORIZED_USER_ID, "persistent", "настойчивый", 1),
                new Word(3, AUTHORIZED_USER_ID, "diligent", "усердный", 5),
                new Word(4, AUTHORIZED_USER_ID, "resilient", "устойчивый", 1),
                new Word(5, AUTHORIZED_USER_ID, "ambitious", "амбициозный", 2)
        );
        mockDictionaryService.addTestWords(AUTHORIZED_USER_ID, testWords);
    }

    /**
     * Тест полного цикла с правильным ответом для слова persistent
     */
    @Test
    public void testFullCycle_CorrectAnswerForPersistent() throws SQLException {
        // Создаем тест
        setupActiveTestForWord("persistent", "настойчивый", 2, 1, "B");

        // Обрабатываем правильный ответ
        String result = oldWord.handleUserAnswer(AUTHORIZED_CHAT_ID, "B");

        String expectedSuccessMessage = "✅ Ваш ответ верный! 😎\nНичего себе вот это память! 🧠🧠🧠\n\n";
        Assertions.assertEquals(expectedSuccessMessage, result,
                "Сообщение об успешном ответе должно соответствовать формату");
        Assertions.assertFalse(oldWord.isTestActive(AUTHORIZED_CHAT_ID), "Тест должен быть завершен");

        // Проверяем что приоритет уменьшился
        Word updatedWord = mockDictionaryService.getWordById(AUTHORIZED_USER_ID, 2);
        Assertions.assertEquals(0, updatedWord.getPriority(), "Приоритет должен уменьшиться с 1 до 0");
    }

    /**
     * Тест полного цикла с неправильным ответом для слова resilient
     */
    @Test
    public void testFullCycle_IncorrectAnswerForResilient() throws SQLException {
        String generatedTestText = "Какое слово означает \"устойчивый\"?\n\n" +
                "A) fragile\n" +
                "B) resilient\n" +
                "C) temporary\n" +
                "D) weak\n\n" +
                "Ответ: B\n" +
                "Слово: resilient - устойчивый";

        // Парсим тест
        OldWordParser parser = new OldWordParser();
        OldWordData testData = parser.parseTest(generatedTestText, "resilient", "устойчивый");
        testData.setWordId(4);
        testData.setCurrentPriority(1);

        // Проверяем что парсер правильно распарсил данные
        Assertions.assertEquals("B", testData.getCorrectAnswer(), "Парсер должен извлечь правильный ответ B");
        Assertions.assertEquals("resilient", testData.getEnglishWord(), "Английское слово должно быть resilient");
        Assertions.assertEquals("устойчивый", testData.getTranslation(), "Перевод должен быть устойчивый");

        // Устанавливаем активный тест
        oldWord.setActiveTest(AUTHORIZED_CHAT_ID, testData);

        // Проверяем неправильный ответ
        boolean isCorrect = oldWord.checkUserAnswer(testData, "A");
        Assertions.assertFalse(isCorrect, "Ответ A должен быть неправильным");

        // Обрабатываем ответ и проверяем результат
        String result = oldWord.handleUserAnswer(AUTHORIZED_CHAT_ID, "A");

        String expectedMessage = "❌ Почти угадали! 😊\n" +
                "Правильный ответ: B\n\n" +
                "📝 Напоминаем перевод слова:\n" +
                "• 🔤 Слово: resilient\n" +
                "• 🌐 Перевод: устойчивый\n\n" +
                "Теперь это слово будет попадаться чаще!";
        Assertions.assertEquals(expectedMessage, result, "Сообщение об ошибке должно соответствовать формату");
        Assertions.assertFalse(oldWord.isTestActive(AUTHORIZED_CHAT_ID), "Тест должен быть завершен");

        // Проверяем что приоритет увеличился
        Word updatedWord = mockDictionaryService.getWordById(AUTHORIZED_USER_ID, 4);
        Assertions.assertEquals(2, updatedWord.getPriority(), "Приоритет должен увеличиться с 1 до 2");
    }

    /**
     * Тест полного цикла с правильным ответом для слова accomplishment
     */
    @Test
    public void testFullCycle_CorrectAnswerForAccomplishment() throws SQLException {
        String generatedTestText = "Какое слово означает \"достижение\"?\n\n" +
                "A) failure\n" +
                "B) accomplishment\n" +
                "C) beginning\n" +
                "D) obstacle\n\n" +
                "Ответ: B\n" +
                "Слово: accomplishment - достижение";

        // Парсим тест напрямую
        OldWordParser parser = new OldWordParser();
        OldWordData testData = parser.parseTest(generatedTestText, "accomplishment", "достижение");
        testData.setWordId(1);
        testData.setCurrentPriority(3);

        // Проверяем что парсер правильно распарсил данные
        Assertions.assertEquals("B", testData.getCorrectAnswer(), "Парсер должен извлечь правильный ответ B");
        Assertions.assertEquals("accomplishment", testData.getEnglishWord(), "Английское слово должно быть accomplishment");
        Assertions.assertEquals("достижение", testData.getTranslation(), "Перевод должен быть достижение");

        // Устанавливаем активный тест
        oldWord.setActiveTest(AUTHORIZED_CHAT_ID, testData);

        // Проверяем правильный ответ
        boolean isCorrect = oldWord.checkUserAnswer(testData, "B");
        Assertions.assertTrue(isCorrect, "Ответ B должен быть правильным");

        // Обрабатываем ответ и проверяем результат
        String result = oldWord.handleUserAnswer(AUTHORIZED_CHAT_ID, "B");

        String expectedSuccessMessage = "✅ Ваш ответ верный! 😎\nНичего себе вот это память! 🧠🧠🧠\n\n";
        Assertions.assertEquals(expectedSuccessMessage, result,
                "Сообщение об успешном ответе должно соответствовать формату");
        Assertions.assertFalse(oldWord.isTestActive(AUTHORIZED_CHAT_ID), "Тест должен быть завершен");

        // Проверяем что приоритет уменьшился
        Word updatedWord = mockDictionaryService.getWordById(AUTHORIZED_USER_ID, 1);
        Assertions.assertEquals(2, updatedWord.getPriority(), "Приоритет должен уменьшиться с 3 до 2");
    }

    /**
     * Тест обработки пустого словаря
     */
    @Test
    public void testStartLowPriorityTest_AuthorizedEmptyDictionary() {
        mockDictionaryService.clearUserWords(AUTHORIZED_USER_ID);

        String result = oldWord.startLowPriorityTest(AUTHORIZED_CHAT_ID);

        Assertions.assertNull(result, "Результат должен быть null при пустом словаре");
        Assertions.assertFalse(oldWord.isTestActive(AUTHORIZED_CHAT_ID), "Тест не должен быть активен при пустом словаре");
    }

    /**
     * Тест обработки неавторизованного пользователя
     */
    @Test
    public void testStartLowPriorityTest_UnauthorizedUser() {
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            oldWord.startLowPriorityTest(UNAUTHORIZED_CHAT_ID);
        });

        String expectedErrorMessage = "Ошибка доступа к словарю: Пользователь не авторизован для chatId: 111222333";
        Assertions.assertEquals(expectedErrorMessage, exception.getMessage(),
                "Сообщение исключения должно указывать на ошибку доступа");
        Assertions.assertFalse(oldWord.isTestActive(UNAUTHORIZED_CHAT_ID),
                "Тест не должен быть активен для неавторизованного пользователя");
    }

    /**
     * Тест очистки активного теста
     */
    @Test
    public void testClearActiveTest() {
        setupActiveTestForWord("test", "тест", 1, 2, "A");
        Assertions.assertTrue(oldWord.isTestActive(AUTHORIZED_CHAT_ID), "Тест должен быть активен до очистки");

        // Очищаем тест
        oldWord.clearActiveTest(AUTHORIZED_CHAT_ID);
        Assertions.assertFalse(oldWord.isTestActive(AUTHORIZED_CHAT_ID), "Тест не должен быть активен после очистки");
    }

    /**
     * Тест обработки ответа без активного теста
     */
    @Test
    public void testHandleUserAnswer_NoActiveTest() {
        String result = oldWord.handleUserAnswer(AUTHORIZED_CHAT_ID, "A");

        String expectedMessage = "❌ Активный тест не найден. Начните тест заново.";
        Assertions.assertEquals(expectedMessage, result,
                "Должно вернуть сообщение об отсутствии активного теста");
        Assertions.assertFalse(oldWord.isTestActive(AUTHORIZED_CHAT_ID), "Тест не должен быть активен");
    }

    /**
     * Тест граничных значений приоритета - минимальное значение
     */
    @Test
    public void testPriorityBoundary_MinimumValue() throws SQLException {
        // Создаем слово с минимальным приоритетом
        mockDictionaryService.clearUserWords(AUTHORIZED_USER_ID);
        mockDictionaryService.addWord(AUTHORIZED_USER_ID, "minword", "минслово", 0);

        // Создаем
        setupActiveTestForWord("minword", "минслово", 1, 0, "B");

        // Обрабатываем правильный ответ
        oldWord.handleUserAnswer(AUTHORIZED_CHAT_ID, "B");

        // Проверяем что приоритет остался 0
        Word updatedWord = mockDictionaryService.getWordById(AUTHORIZED_USER_ID, 1);
        Assertions.assertEquals(0, updatedWord.getPriority(), "Приоритет должен остаться 0 (минимальное значение)");
    }

    /**
     * Тест граничных значений приоритета - максимальное значение
     */
    @Test
    public void testPriorityBoundary_MaximumValue() throws SQLException {
        // Создаем слово с максимальным приоритетом
        mockDictionaryService.clearUserWords(AUTHORIZED_USER_ID);
        mockDictionaryService.addWord(AUTHORIZED_USER_ID, "maxword", "максслово", 10);

        // Создаем тест
        setupActiveTestForWord("maxword", "максслово", 1, 10, "B");

        // Обрабатываем неправильный ответ
        oldWord.handleUserAnswer(AUTHORIZED_CHAT_ID, "A");

        // Проверяем что приоритет остался 10
        Word updatedWord = mockDictionaryService.getWordById(AUTHORIZED_USER_ID, 1);
        Assertions.assertEquals(10, updatedWord.getPriority(), "Приоритет должен остаться 10 (максимальное значение)");
    }

    /**
     * Вспомогательный метод для настройки активного теста вручную
     */
    private void setupActiveTestForWord(String englishWord, String translation, int wordId, int priority, String correctAnswer) {
        // Создаем тестовый текст
        String testText = String.format("Какое слово означает \"%s\"?\n\nA) вариант1\nB) %s\nC) вариант3\nD) вариант4\nОтвет: %s",
                translation, englishWord, correctAnswer);

        // Создаем OldWordData
        OldWordData testData = new OldWordData(testText, correctAnswer, englishWord, translation);
        testData.setWordId(wordId);
        testData.setCurrentPriority(priority);

        // Устанавливаем активный тест через публичный метод
        oldWord.setActiveTest(AUTHORIZED_CHAT_ID, testData);
    }
}