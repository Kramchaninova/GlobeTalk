package org.example;

import org.example.ScheduledNewWord.Message;
import org.example.ScheduledNewWord.MessageParser;
import org.example.ScheduledNewWord.ScheduleGenerateMessage;
import org.example.Dictionary.DictionaryService;
import org.example.Dictionary.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.util.*;

/**
 * Комплексные тесты для класса Message с мокированием DictionaryService
 * Тестирует генерацию уникальных слов и обработку кнопок пользователя
 */
public class MessageTest {

    private Message message;
    private MockDictionaryService mockDictionaryService;
    private TestScheduleGenerateMessage testGenerator;
    private MessageParser realParser;

    private final long AUTHORIZED_USER_ID = 12345L;
    private final long AUTHORIZED_CHAT_ID = 67890L;
    private final long UNAUTHORIZED_CHAT_ID = 111222333L;

    /**
     * Мок-реализация DictionaryService для тестирования Message
     * Имитирует работу с базой данных слов пользователя
     */
    private class MockDictionaryService implements DictionaryService {
        private final Map<Long, List<Word>> userWords = new HashMap<>();
        private int nextWordId = 1;

        /**
         * Добавляет тестовые слова для пользователя
         * @param userId идентификатор пользователя
         * @param words список слов для добавления
         */
        public void addTestWords(long userId, List<Word> words) {
            userWords.put(userId, new ArrayList<>(words));
        }

        @Override
        public long getUserIdByChatId(long chatId) throws SQLException {
            if (chatId == AUTHORIZED_CHAT_ID) {
                return AUTHORIZED_USER_ID;
            }
            throw new SQLException("Пользователь не авторизован");
        }

        @Override
        public List<Word> getAllWords(long userId) throws SQLException {
            return userWords.getOrDefault(userId, Collections.emptyList());
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
                throw new SQLException("Слово уже существует");
            }

            Word newWord = new Word(nextWordId++, userId, englishWord, translation, priority);
            words.add(newWord);
        }

        @Override
        public void updateWord(long userId, int wordId, String newEnglishWord, String newTranslation, Integer newPriority) throws SQLException {
            List<Word> words = userWords.get(userId);
            if (words == null) throw new SQLException("Словарь не найден");

            for (int i = 0; i < words.size(); i++) {
                Word word = words.get(i);
                if (word.getId() == wordId) {
                    Word updatedWord = new Word(wordId, userId, newEnglishWord, newTranslation,
                            newPriority != null ? newPriority : word.getPriority());
                    words.set(i, updatedWord);
                    return;
                }
            }
            throw new SQLException("Слово не найдено");
        }

        @Override
        public void updateWordPriority(long userId, int wordId, int newPriority) throws SQLException {
            updateWord(userId, wordId, null, null, newPriority);
        }

        @Override
        public void deleteWord(long userId, int wordId) throws SQLException {
            List<Word> words = userWords.get(userId);
            if (words == null) throw new SQLException("Словарь не найден");

            boolean removed = words.removeIf(word -> word.getId() == wordId);
            if (!removed) throw new SQLException("Слово не найдено");
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

    /**
     * Тестовый генератор слов
     */
    private class TestScheduleGenerateMessage extends ScheduleGenerateMessage {
        private final List<String> testWords = Arrays.asList(
                "WORD: persistent\nTRANSLATION: настойчивый\nLEVEL: B2\nPART_OF_SPEECH: adjective\nEXAMPLE: She is very persistent in her work.\nEXAMPLE_TRANSLATION: Она очень настойчивая в своей работе.",
                "WORD: resilient\nTRANSLATION: устойчивый\nLEVEL: B2\nPART_OF_SPEECH: adjective\nEXAMPLE: He is very resilient to stress.\nEXAMPLE_TRANSLATION: Он очень устойчив к стрессу.",
                "WORD: diligent\nTRANSLATION: усердный\nLEVEL: B1\nPART_OF_SPEECH: adjective\nEXAMPLE: She is a diligent student.\nEXAMPLE_TRANSLATION: Она усердная студентка.",
                "WORD: ambitious\nTRANSLATION: амбициозный\nLEVEL: B2\nPART_OF_SPEECH: adjective\nEXAMPLE: He has ambitious goals.\nEXAMPLE_TRANSLATION: У него амбициозные цели.",
                "WORD: accomplishment\nTRANSLATION: достижение\nLEVEL: B2\nPART_OF_SPEECH: noun\nEXAMPLE: This is a great accomplishment.\nEXAMPLE_TRANSLATION: Это великое достижение."
        );

        private int currentIndex = 0;

        @Override
        public String generateWord() {
            // Возвращаем тестовые данные по кругу
            String word = testWords.get(currentIndex);
            currentIndex = (currentIndex + 1) % testWords.size(); // Зацикливаем
            return word;
        }

        /**
         * Сбрасывает индекс для предсказуемости тестов
         */
        public void resetIndex() {
            currentIndex = 0;
        }
    }

    /**
     * Настройка тестового окружения перед каждым тестом
     */
    @BeforeEach
    public void setUp() {
        mockDictionaryService = new MockDictionaryService();
        testGenerator = new TestScheduleGenerateMessage();
        realParser = new MessageParser(); // Используем реальный парсер

        message = new Message(mockDictionaryService, testGenerator, realParser);

        // Сбрасываем индекс генератора для предсказуемости
        testGenerator.resetIndex();
    }

    /**
     * Тест успешной генерации уникального слова для авторизованного пользователя
     * Проверяет что слово генерируется, добавляется в словарь и форматируется правильно
     */
    @Test
    public void testGetUniqueWordForUser_Success() throws SQLException {
        String result = message.getUniqueWordForUser(AUTHORIZED_CHAT_ID);

        // Проверяем структуру ответа
        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertTrue(result.contains("🎉 **Новое слово!** 🎉"), "Должен быть заголовок нового слова");
        Assertions.assertTrue(result.contains("persistent"), "Должно содержать английское слово");
        Assertions.assertTrue(result.contains("настойчивый"), "Должно содержать перевод");

        // Проверяем что слово добавилось в словарь
        Word addedWord = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "persistent");
        Assertions.assertNotNull(addedWord, "Слово должно быть добавлено в словарь");
        Assertions.assertEquals(5, addedWord.getPriority(), "Слово должно добавляться с приоритетом 5");
        Assertions.assertEquals("persistent", addedWord.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("настойчивый", addedWord.getTranslation(), "Перевод должен совпадать");
    }

    /**
     * Тест обработки кнопки "Знаю" для существующего слова
     * Проверяет что приоритет слова меняется на 2 и генерируется новое слово
     */
    @Test
    public void testHandleWordButtonClick_KnowButton() throws SQLException {
        // Сначала получаем слово
        message.getUniqueWordForUser(AUTHORIZED_CHAT_ID);

        // Обрабатываем кнопку "Знаю"
        String result = message.handleWordButtonClick("know_button", AUTHORIZED_CHAT_ID);

        String expectedStart = "✅Здорово!";
        Assertions.assertEquals(expectedStart, result.substring(0, expectedStart.length()));

        // Проверяем что приоритет обновился
        Word updatedWord = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "persistent");
        Assertions.assertEquals(2, updatedWord.getPriority(), "Приоритет должен уменьшиться до 2 после нажатия 'Знаю'");
    }

    /**
     * Тест обработки кнопки "Изучаю" для существующего слова
     * Проверяет что слово остается с приоритетом 5 и возвращается корректное сообщение
     */
    @Test
    public void testHandleWordButtonClick_LearnButton() throws SQLException {
        // Сначала получаем слово
        message.getUniqueWordForUser(AUTHORIZED_CHAT_ID);

        // Обрабатываем кнопку "Изучаю"
        String result = message.handleWordButtonClick("learn_button", AUTHORIZED_CHAT_ID);

        String expectedMessage = "✅ Слово уже добавлено в словарь для изучения!\n" +
                "Посмотрите все слова в словаре или изучайте еще...";
        Assertions.assertEquals(expectedMessage, result);

        // Проверяем что приоритет остался 5
        Word word = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "persistent");
        Assertions.assertEquals(5, word.getPriority(), "Приоритет должен остаться 5 после нажатия 'Изучаю'");
    }

    /**
     * Тест поведения для неавторизованного пользователя
     * Проверяет что возвращается корректное сообщение об ошибке
     */
    @Test
    public void testGetUniqueWordForUser_Unauthorized() {
        String result = message.getUniqueWordForUser(UNAUTHORIZED_CHAT_ID);

        String expectedMessage = "❌ Ошибка при проверке словаря. Попробуйте позже.";
        Assertions.assertEquals(expectedMessage, result);
    }

    /**
     * Тест обработки кнопки без активного слова
     * Проверяет что возвращается сообщение о необходимости сначала получить слово
     */
    @Test
    public void testHandleWordButtonClick_NoActiveWord() {
        String result = message.handleWordButtonClick("know_button", AUTHORIZED_CHAT_ID);

        String expectedMessage = "❌ Нет активного слова для обработки. Сначала получите слово через /word";
        Assertions.assertEquals(expectedMessage, result);
    }

    /**
     * Тест обработки кнопки "Еще слово" для получения нового слова
     * Проверяет что при нажатии кнопки генерируется новое слово
     */
    @Test
    public void testHandleWordButtonClick_MoreWordButton() throws SQLException {
        // Сначала получаем слово
        message.getUniqueWordForUser(AUTHORIZED_CHAT_ID);

        // Запоминаем количество слов до нажатия кнопки
        int wordsBefore = mockDictionaryService.getAllWords(AUTHORIZED_USER_ID).size();

        // Обрабатываем кнопку "Еще слово"
        String result = message.handleWordButtonClick("more_word_button", AUTHORIZED_CHAT_ID);

        String expectedStart = "🎉 **Новое слово!** 🎉";
        Assertions.assertEquals(expectedStart, result.substring(0, expectedStart.length()));

        // Проверяем что добавилось новое слово
        int wordsAfter = mockDictionaryService.getAllWords(AUTHORIZED_USER_ID).size();
        Assertions.assertEquals(wordsBefore + 1, wordsAfter, "Должно добавиться новое слово");

        // Проверяем что второе слово другое
        Word secondWord = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "resilient");
        Assertions.assertNotNull(secondWord, "Второе слово должно быть добавлено в словарь");
        Assertions.assertEquals("resilient", secondWord.getEnglishWord(), "Второе слово должно быть другим");
    }

    /**
     * Тест обработки неизвестной команды кнопки
     * Проверяет что возвращается сообщение о неизвестной команде
     */
    @Test
    public void testHandleWordButtonClick_UnknownButton() throws SQLException {
        // Сначала получаем слово
        message.getUniqueWordForUser(AUTHORIZED_CHAT_ID);

        String result = message.handleWordButtonClick("unknown_button", AUTHORIZED_CHAT_ID);

        String expectedMessage = "❌ Неизвестная команда кнопки";
        Assertions.assertEquals(expectedMessage, result);
    }

    /**
     * Тест проверки логики приоритетов при нажатии кнопки "Знаю"
     * Проверяет что приоритет слова уменьшается с 5 до 2
     */
    @Test
    public void testWordPriority_KnowButtonDecreasesPriority() throws SQLException {
        // Получаем слово - оно должно добавиться с приоритетом 5
        message.getUniqueWordForUser(AUTHORIZED_CHAT_ID);

        Word wordBefore = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "persistent");
        Assertions.assertEquals(5, wordBefore.getPriority(), "Слово должно добавляться с приоритетом 5");

        // Нажимаем "Знаю" - приоритет должен уменьшиться до 2
        message.handleWordButtonClick("know_button", AUTHORIZED_CHAT_ID);

        Word wordAfter = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "persistent");
        Assertions.assertEquals(2, wordAfter.getPriority(), "Приоритет должен уменьшиться до 2 после нажатия 'Знаю'");
    }

    /**
     * Тест что слово действительно уникальное (отсутствует в словаре перед генерацией)
     */
    @Test
    public void testGetUniqueWordForUser_WordIsUnique() throws SQLException {
        // Проверяем что словаря изначально пуст
        List<Word> wordsBefore = mockDictionaryService.getAllWords(AUTHORIZED_USER_ID);
        Assertions.assertTrue(wordsBefore.isEmpty(), "Словарь должен быть пуст перед тестом");

        String result = message.getUniqueWordForUser(AUTHORIZED_CHAT_ID);

        // Проверяем что слово добавилось
        List<Word> wordsAfter = mockDictionaryService.getAllWords(AUTHORIZED_USER_ID);
        Assertions.assertFalse(wordsAfter.isEmpty(), "Уникальное слово должно быть добавлено в словарь");
        Assertions.assertEquals(1, wordsAfter.size(), "Должно быть добавлено ровно одно слово");
    }
}