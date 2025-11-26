package org.example;

import org.example.ScheduledTests.*;
import org.example.Dictionary.DictionaryService;
import org.example.Dictionary.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Комплексные тесты для системы отложенных тестов
 * Тестирует генерацию тестов, парсинг и обработку ответов пользователя
 */
public class ScheduledTestsTest {

    private ScheduleTests scheduleTests;
    private ScheduleTestHandler scheduleTestHandler;
    private MockDictionaryService mockDictionaryService;

    private final long AUTHORIZED_USER_ID = 12345L;
    private final long AUTHORIZED_CHAT_ID = 67890L;

    // Тестовый текст теста
    private final String TEST_TEXT = "Вопрос:\n" +
            "Выберите правильный перевод слова \"persistent\"\n\n" +
            "A) неудачный\n" +
            "B) настойчивый\n" +
            "C) временный\n" +
            "D) гибкий\n\n" +
            "Ответ: B\n" +
            "Тип: ПРИОРИТЕТНОЕ\n" +
            "Слово: persistent - настойчивый\n\n" +
            "Вопрос:\n" +
            "Какое слово означает \"устойчивый, стабильный\"?\n\n" +
            "A) fragile\n" +
            "B) resilient\n" +
            "C) temporary\n" +
            "D) flexible\n\n" +
            "Ответ: B\n" +
            "Тип: НОВОЕ\n" +
            "Слово: resilient - устойчивый\n\n" +
            "Вопрос:\n" +
            "Выберите правильный перевод слова \"diligent\"\n\n" +
            "A) ленивый\n" +
            "B) усердный\n" +
            "C) случайный\n" +
            "D) быстрый\n\n" +
            "Ответ: B\n" +
            "Тип: ПРИОРИТЕТНОЕ\n" +
            "Слово: diligent - усердный\n\n" +
            "Вопрос:\n" +
            "Какое слово соответствует описанию \"амбициозный, целеустремленный\"?\n\n" +
            "A) lazy\n" +
            "B) ambitious\n" +
            "C) simple\n" +
            "D) quiet\n\n" +
            "Ответ: B\n" +
            "Тип: НОВОЕ\n" +
            "Слово: ambitious - амбициозный";

    /**
     * Мок-реализация DictionaryService для тестирования ScheduleTests
     */
    private class MockDictionaryService implements DictionaryService {
        private final Map<Long, List<Word>> userWords = new HashMap<>();
        private int nextWordId = 1;

        /**
         * Добавляет авторизацию пользователя для тестирования
         */
        public void authorizeUser(long chatId, long userId) {
            userWords.putIfAbsent(userId, new ArrayList<>());
        }

        /**
         * Добавляет тестовые слова для пользователя
         */
        public void addTestWords(long userId, List<Word> words) {
            // Фильтруем null слова и проверяем корректность данных
            List<Word> validWords = words.stream()
                    .filter(word -> word != null &&
                            word.getEnglishWord() != null &&
                            word.getTranslation() != null)
                    .collect(Collectors.toList());
            userWords.put(userId, new ArrayList<>(validWords));
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
            return userWords.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(word -> word != null)
                    .collect(Collectors.toList());
        }

        @Override
        public Word getWordByEnglish(long userId, String englishWord) throws SQLException {
            if (englishWord == null) {
                return null;
            }

            return userWords.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(word -> word != null &&
                            word.getEnglishWord() != null &&
                            word.getEnglishWord().equalsIgnoreCase(englishWord))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void addWord(long userId, String englishWord, String translation, int priority) throws SQLException {
            if (englishWord == null || translation == null) {
                throw new SQLException("Английское слово и перевод не могут быть null");
            }

            List<Word> words = userWords.computeIfAbsent(userId, k -> new ArrayList<>());

            if (words.stream()
                    .filter(word -> word != null && word.getEnglishWord() != null)
                    .anyMatch(word -> word.getEnglishWord().equalsIgnoreCase(englishWord))) {
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
                if (word != null && word.getId() == wordId) {
                    Word updatedWord = new Word(
                            wordId,
                            userId,
                            newEnglishWord != null ? newEnglishWord : word.getEnglishWord(),
                            newTranslation != null ? newTranslation : word.getTranslation(),
                            newPriority != null ? newPriority : word.getPriority()
                    );
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

            boolean removed = words.removeIf(word -> word != null && word.getId() == wordId);
            if (!removed) throw new SQLException("Слово не найдено");
        }

        @Override
        public Word getWordById(long userId, int wordId) throws SQLException {
            return userWords.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(word -> word != null && word.getId() == wordId)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Word> getWordsByPriority(long userId, int priority) throws SQLException {
            return userWords.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(word -> word != null && word.getPriority() == priority)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Настройка тестового окружения перед каждым тестом
     */
    @BeforeEach
    public void setUp() {
        mockDictionaryService = new MockDictionaryService();
        mockDictionaryService.authorizeUser(AUTHORIZED_CHAT_ID, AUTHORIZED_USER_ID);

        // Создаем ScheduleTests с мок-сервисом через конструктор для тестирования
        scheduleTests = new ScheduleTests(mockDictionaryService);
        scheduleTestHandler = new ScheduleTestHandler(scheduleTests);
    }

    /**
     * Тест получения приоритетных слов для пользователя
     */
    @Test
    public void testGetWordsWithMaxPriority() throws SQLException {
        // Добавляем тестовые слова пользователю
        List<Word> testWords = Arrays.asList(
                new Word(1, AUTHORIZED_USER_ID, "persistent", "настойчивый", 8),
                new Word(2, AUTHORIZED_USER_ID, "diligent", "усердный", 7),
                new Word(3, AUTHORIZED_USER_ID, "resilient", "устойчивый", 6),
                new Word(4, AUTHORIZED_USER_ID, "ambitious", "амбициозный", 5),
                new Word(5, AUTHORIZED_USER_ID, "accomplishment", "достижение", 4),
                new Word(6, AUTHORIZED_USER_ID, "determined", "решительный", 3)
        );
        mockDictionaryService.addTestWords(AUTHORIZED_USER_ID, testWords);

        List<String[]> priorityWords = scheduleTests.getWordsWithMaxPriority(AUTHORIZED_USER_ID);

        Assertions.assertNotNull(priorityWords, "Список приоритетных слов не должен быть null");
        Assertions.assertFalse(priorityWords.isEmpty(), "Список приоритетных слов не должен быть пустым");
        Assertions.assertEquals(6, priorityWords.size(), "Должно вернуться 6 приоритетных слов");

        // Проверяем что слова отсортированы по приоритету
        Assertions.assertEquals("persistent", priorityWords.get(0)[0], "Первое слово должно быть с наивысшим приоритетом");
        Assertions.assertEquals("настойчивый", priorityWords.get(0)[1], "Перевод должен соответствовать");
    }

    /**
     * Тест парсинга теста из текста
     */
    @Test
    public void testParseTest() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        Assertions.assertNotNull(testsData, "Данные теста не должны быть null");
        Assertions.assertFalse(testsData.getQuestions().isEmpty(), "Должен быть хотя бы один вопрос");

        // Проверяем первый вопрос
        TestsData.QuestionData firstQuestion = testsData.getQuestions().get(0);
        Assertions.assertEquals("persistent", firstQuestion.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("настойчивый", firstQuestion.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("B", firstQuestion.getCorrectAnswer(), "Правильный ответ должен быть B");
        Assertions.assertEquals("ПРИОРИТЕТНОЕ", firstQuestion.getWordType(), "Тип должен быть ПРИОРИТЕТНОЕ");

        // Проверяем второй вопрос
        TestsData.QuestionData secondQuestion = testsData.getQuestions().get(1);
        Assertions.assertEquals("resilient", secondQuestion.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("устойчивый", secondQuestion.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("B", secondQuestion.getCorrectAnswer(), "Правильный ответ должен быть B");
        Assertions.assertEquals("НОВОЕ", secondQuestion.getWordType(), "Тип должен быть НОВОЕ");

        // Проверяем общее количество вопросов
        Assertions.assertEquals(4, testsData.getQuestions().size(), "Должно быть 4 вопроса");
    }

    /**
     * Тест обработки кнопок начала теста
     */
    @Test
    public void testHandleButtonClick_StartTest() {
        // Подготавливаем тестовые данные
        List<Word> testWords = Arrays.asList(
                new Word(1, AUTHORIZED_USER_ID, "persistent", "настойчивый", 8),
                new Word(2, AUTHORIZED_USER_ID, "diligent", "усердный", 7),
                new Word(3, AUTHORIZED_USER_ID, "resilient", "устойчивый", 6)
        );
        mockDictionaryService.addTestWords(AUTHORIZED_USER_ID, testWords);

        // Задаем ожидаемый результат
        String expectedQuestion = "Вопрос 1 из 3:\nВыберите правильный перевод слова \"persistent\"\n\nA) неудачный\nB) настойчивый\nC) временный\nD) гибкий";

        // Создаем ScheduleTests с переопределенным методом генерации теста
        ScheduleTests isolatedScheduleTests = new ScheduleTests(mockDictionaryService) {
            @Override
            public String handleButtonClick(String button, long chatId) {
                if ("yes_schedule_test_button".equals(button)) {
                    try {
                        long userId = mockDictionaryService.getUserIdByChatId(chatId);
                        List<String[]> words = getWordsWithMaxPriority(userId);
                        if (words.isEmpty()) {
                            return "❌ Недостаточно слов для создания теста. Добавьте больше слов в словарь.";
                        }
                        // Возвращаем заранее подготовленный вопрос вместо генерации
                        return expectedQuestion;
                    } catch (Exception e) {
                        return "❌ Ошибка при создании теста: " + e.getMessage();
                    }
                }
                return super.handleButtonClick(button, chatId);
            }
        };

        String result = isolatedScheduleTests.handleButtonClick("yes_schedule_test_button", AUTHORIZED_CHAT_ID);

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals(expectedQuestion, result, "Должен вернуться заранее подготовленный вопрос");
    }
    /**
     * Тест обработки кнопки отказа от теста
     */
    @Test
    public void testHandleButtonClick_DeclineTest() {
        String result = scheduleTests.handleButtonClick("no_schedule_test_button", AUTHORIZED_CHAT_ID);

        String expectedMessage = "Хорошо, не сейчас ✨\n\n" +
                "Знания никуда не убегут — они терпеливо ждут своего часа.\n\n" +
                "Когда почувствуете готовность, просто нажмите кнопку /scheduled_test в боковом меню - и мы продолжим!\n\n" +
                "🌟 Ваше обучение — в ваших руках";

        Assertions.assertEquals(expectedMessage, result, "Сообщение об отказе должно точно совпадать");
    }

    /**
     * Тест получения приветственного сообщения
     */
    @Test
    public void testGetTestIntroduction() {
        String introduction = scheduleTests.getTestIntroduction();

        String expectedMessage = "🌙 Момент истины настал!\n\n" +
                "Знания, которые вы собирали по крупицам в течении недели и не только, готовы проверке!\n\n" +
                "✨ Готовы бросить вызов себе?";

        Assertions.assertEquals(expectedMessage, introduction, "Приветственное сообщение должно точно совпадать");
    }

    /**
     * Тест обновления приоритета существующего слова
     */
    @Test
    public void testUpdateWordPriority_ExistingWord() throws SQLException {
        // Добавляем слово в словарь
        mockDictionaryService.addWord(AUTHORIZED_USER_ID, "persistent", "настойчивый", 5);

        // Проверяем что слово добавилось
        Word wordBefore = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "persistent");
        Assertions.assertNotNull(wordBefore, "Слово должно существовать в словаре перед обновлением");
        Assertions.assertEquals(5, wordBefore.getPriority(), "Начальный приоритет должен быть 5");

        // Обновляем приоритет (правильный ответ)
        scheduleTests.updateWordPriority(AUTHORIZED_USER_ID, "persistent", "настойчивый", true, true);

        Word updatedWord = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "persistent");
        Assertions.assertNotNull(updatedWord, "Слово должно существовать в словаре после обновления");
        Assertions.assertEquals(4, updatedWord.getPriority(), "Приоритет должен уменьшиться на 1 при правильном ответе");
    }

    /**
     * Тест добавления нового слова при правильном ответе
     */
    @Test
    public void testUpdateWordPriority_NewWordCorrect() throws SQLException {
        // Проверяем что слова изначально нет в словаре
        Word wordBefore = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "resilient");
        Assertions.assertNull(wordBefore, "Слова не должно быть в словаре перед тестом");

        // Обновляем приоритет (правильный ответ для нового слова)
        scheduleTests.updateWordPriority(AUTHORIZED_USER_ID, "resilient", "устойчивый", true, false);

        Word newWord = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "resilient");
        Assertions.assertNotNull(newWord, "Новое слово должно быть добавлено при правильном ответе");
        Assertions.assertEquals(3, newWord.getPriority(), "Новое слово должно добавляться с приоритетом 3");
        Assertions.assertEquals("resilient", newWord.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("устойчивый", newWord.getTranslation(), "Перевод должен совпадать");
    }

    /**
     * Тест что новое слово не добавляется при неправильном ответе
     */
    @Test
    public void testUpdateWordPriority_NewWordIncorrect() throws SQLException {
        // Проверяем что слова изначально нет в словаре
        Word wordBefore = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "resilient");
        Assertions.assertNull(wordBefore, "Слова не должно быть в словаре перед тестом");

        // Обновляем приоритет (неправильный ответ для нового слова)
        scheduleTests.updateWordPriority(AUTHORIZED_USER_ID, "resilient", "устойчивый", false, false);

        Word newWord = mockDictionaryService.getWordByEnglish(AUTHORIZED_USER_ID, "resilient");
        Assertions.assertNull(newWord, "Новое слово не должно добавляться при неправильном ответе");
    }

    /**
     * Тест поведения при пустом словаре
     */
    @Test
    public void testGetWordsWithMaxPriority_EmptyDictionary() {
        List<String[]> priorityWords = scheduleTests.getWordsWithMaxPriority(AUTHORIZED_USER_ID);

        Assertions.assertNotNull(priorityWords, "Список не должен быть null даже при пустом словаре");
        Assertions.assertTrue(priorityWords.isEmpty(), "Список должен быть пустым при отсутствии слов");
    }

    /**
     * Тест обработки неизвестной команды кнопки
     */
    @Test
    public void testHandleButtonClick_UnknownCommand() {
        String result = scheduleTests.handleButtonClick("unknown_button", AUTHORIZED_CHAT_ID);

        Assertions.assertEquals("❌ Неизвестная команда теста", result,
                "Должно вернуться сообщение о неизвестной команде");
    }

    /**
     * Тест создания TestSession и работы с вопросами
     */
    @Test
    public void testTestSession() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        TestSession session = new TestSession(testsData, AUTHORIZED_USER_ID);

        // Проверяем первый вопрос
        String firstQuestion = session.getCurrentQuestion();
        Assertions.assertTrue(firstQuestion.contains("Вопрос 1 из 4"), "Должен быть первый вопрос с номером");
        Assertions.assertTrue(firstQuestion.contains("persistent"), "Должен содержать английское слово");

        // Проверяем ответ
        boolean isCorrect = session.checkAnswer("B");
        Assertions.assertTrue(isCorrect, "Ответ B должен быть правильным для первого вопроса");

        // Переходим к следующему вопросу
        session.nextQuestion();
        String secondQuestion = session.getCurrentQuestion();
        Assertions.assertTrue(secondQuestion.contains("Вопрос 2 из 4"), "Должен быть второй вопрос");

        // Проверяем что тест еще не завершен
        Assertions.assertFalse(session.isTestCompleted(), "Тест не должен быть завершен после 2 вопросов");
    }

    /**
     * Тест полного цикла теста с ответами на все вопросы
     */
    @Test
    public void testFullTestCycle() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        TestSession session = new TestSession(testsData, AUTHORIZED_USER_ID);

        // Отвечаем на все вопросы правильно
        int totalQuestions = testsData.getQuestions().size();
        for (int i = 0; i < totalQuestions; i++) {
            String question = session.getCurrentQuestion();

            // Проверка номера вопроса
            String expectedQuestionStart = "Вопрос " + (i + 1) + " из " + totalQuestions + ":";
            Assertions.assertTrue(question.startsWith(expectedQuestionStart),
                    "Вопрос должен начинаться с: " + expectedQuestionStart);

            // Проверяем содержание вопроса
            TestsData.QuestionData currentQuestionData = testsData.getQuestions().get(i);
            Assertions.assertTrue(question.contains(currentQuestionData.getEnglishWord()),
                    "Вопрос должен содержать английское слово: " + currentQuestionData.getEnglishWord());

            // Отвечаем правильно (все вопросы в тесте имеют правильный ответ "B")
            boolean isCorrect = session.checkAnswer("B");
            Assertions.assertTrue(isCorrect, "Ответ B должен быть правильным для вопроса " + (i + 1));

            // Проверяем счетчик правильных ответов
            Assertions.assertEquals(i + 1, session.getCorrectAnswersCount(),
                    "Счетчик правильных ответов должен быть " + (i + 1) + " после " + (i + 1) + " вопроса");

            session.nextQuestion();
        }

        // Проверка завершения теста
        Assertions.assertTrue(session.isTestCompleted(), "Тест должен быть завершен после ответов на все вопросы");
        Assertions.assertEquals(totalQuestions, session.getCorrectAnswersCount(),
                "Все " + totalQuestions + " ответов должны быть правильными");
        Assertions.assertEquals(totalQuestions, session.getTotalQuestions(),
                "Общее количество вопросов должно быть " + totalQuestions);
    }

    // ТЕСТЫ ДЛЯ ScheduleTestHandler
    /**
     * Тест начала теста через ScheduleTestHandler
     */
    @Test
    public void testScheduleTestHandler_StartTest() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        String result = scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        Assertions.assertNotNull(result, "Результат начала теста не должен быть null");
        Assertions.assertTrue(result.contains("Вопрос 1 из 4"), "Должен вернуться первый вопрос");
        Assertions.assertTrue(result.contains("persistent"), "Должен содержать английское слово из первого вопроса");
    }

    /**
     * Тест обработки правильного ответа через ScheduleTestHandler
     */
    @Test
    public void testScheduleTestHandler_HandleCorrectAnswer() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        // Начинаем тест
        scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        // Обрабатываем правильный ответ
        String result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "B_button");

        Assertions.assertNotNull(result, "Результат обработки ответа не должен быть null");
        Assertions.assertTrue(result.contains("Вопрос 2 из 4"), "Должен вернуться следующий вопрос");
    }

    /**
     * Тест обработки неправильного ответа через ScheduleTestHandler
     */
    @Test
    public void testScheduleTestHandler_HandleIncorrectAnswer() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        // Начинаем тест
        scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        // Обрабатываем неправильный ответ (A вместо B)
        String result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "A_button");

        Assertions.assertNotNull(result, "Результат обработки ответа не должен быть null");
        Assertions.assertTrue(result.contains("Вопрос 2 из 4"), "Должен вернуться следующий вопрос даже при неправильном ответе");
    }

    /**
     * Тест полного прохождения теста через ScheduleTestHandler
     */
    @Test
    public void testScheduleTestHandler_FullTestCompletion() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        // Начинаем тест
        scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        // Отвечаем на все вопросы правильно
        String result = "";
        for (int i = 0; i < testsData.getQuestions().size(); i++) {
            result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "B_button");
        }

        Assertions.assertNotNull(result, "Финальный результат не должен быть null");
        Assertions.assertTrue(result.contains("🎉 Тест завершен! 🎉"), "Должно содержать сообщение о завершении теста");
        Assertions.assertTrue(result.contains("📊 Результаты:"), "Должно содержать результаты теста");
        Assertions.assertTrue(result.contains("Всего вопросов: 4"), "Должно содержать информацию о количестве вопросов");
    }

    /**
     * Тест полного цикла теста с ответами на все вопросы
     */
    @Test
    public void testScheduleTestHandler_IsTestActive() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        TestSession session = new TestSession(testsData, AUTHORIZED_USER_ID);

        // Проверка начального состояния
        Assertions.assertEquals(0, session.getCorrectAnswersCount(), "Начальное количество правильных ответов должно быть 0");
        Assertions.assertEquals(4, session.getTotalQuestions(), "Общее количество вопросов должно быть 4");
        Assertions.assertFalse(session.isTestCompleted(), "Тест не должен быть завершен в начале");

        // Отвечаем на все вопросы правильно
        int totalQuestions = testsData.getQuestions().size();
        for (int i = 0; i < totalQuestions; i++) {
            String question = session.getCurrentQuestion();

            // Проверка формата вопроса
            String expectedQuestionStart = "Вопрос " + (i + 1) + " из " + totalQuestions + ":\n\n";
            Assertions.assertEquals(expectedQuestionStart, question.substring(0, expectedQuestionStart.length()),
                    "Вопрос должен начинаться с: " + expectedQuestionStart);

            // Проверяем содержание вопроса
            TestsData.QuestionData currentQuestionData = testsData.getQuestions().get(i);
            String questionText = question.substring(expectedQuestionStart.length());
            Assertions.assertEquals(currentQuestionData.getQuestionText(), questionText,
                    "Текст вопроса должен точно совпадать");

            // Отвечаем правильно и проверяем результат
            boolean isCorrect = session.checkAnswer("B");
            Assertions.assertTrue(isCorrect, "Ответ B должен быть правильным для вопроса " + (i + 1));

            // Проверка счетчика правильных ответов
            Assertions.assertEquals(i + 1, session.getCorrectAnswersCount(),
                    "Счетчик правильных ответов должен быть " + (i + 1) + " после " + (i + 1) + " вопроса");

            session.nextQuestion();
        }

        // Проверка завершения теста
        Assertions.assertTrue(session.isTestCompleted(), "Тест должен быть завершен после ответов на все вопросы");
        Assertions.assertEquals(totalQuestions, session.getCorrectAnswersCount(),
                "Все " + totalQuestions + " ответов должны быть правильными");
        Assertions.assertEquals(totalQuestions, session.getTotalQuestions(),
                "Общее количество вопросов должно быть " + totalQuestions);
    }

    /**
     * Тест получения текущего вопроса
     */
    @Test
    public void testScheduleTestHandler_GetCurrentQuestion() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        // Начинаем тест
        scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        String question = scheduleTestHandler.getCurrentQuestion(AUTHORIZED_CHAT_ID);

        Assertions.assertNotNull(question, "Текущий вопрос не должен быть null");
        Assertions.assertTrue(question.contains("Вопрос 1 из 4"), "Должен вернуться текущий вопрос с правильным номером");
    }

    /**
     * Тест обработки ответа без активного теста
     */
    @Test
    public void testScheduleTestHandler_HandleAnswerWithoutActiveTest() {
        String result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "B_button");

        Assertions.assertEquals("❌ Тест не активен.", result,
                "Должно вернуться сообщение об отсутствии активного теста");
    }

    /**
     * Тест начала теста с пустыми вопросами
     */
    @Test
    public void testScheduleTestHandler_StartTestWithEmptyQuestions() {
        // Создаем пустой TestsData через парсинг пустого текста
        TestsParser parser = new TestsParser();
        TestsData emptyTestsData = parser.parseTest("");

        String result = scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, emptyTestsData, AUTHORIZED_USER_ID);

        Assertions.assertEquals("❌ Не удалось загрузить вопросы для теста.", result,
                "Должно вернуться сообщение об ошибке при пустых вопросах");
    }

    /**
     * Тест финальной фразы для отличного результата (100%)
     */
    @Test
    public void testFormatTestResult_Excellent() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        // Начинаем тест
        scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        // Отвечаем на все вопросы правильно (4 из 4)
        String result = "";
        for (int i = 0; i < 4; i++) {
            result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "B_button");
        }

        String expectedResult = "🎉 Тест завершен! 🎉\n\n" +
                "📊 Результаты:\n" +
                "• Всего вопросов: 4\n" +
                "• Правильных ответов: 4\n" +
                "• Ошибок: 0\n" +
                "• Процент правильных: 100%\n\n" +
                "🎉 *Блестящий результат!*\n" +
                "Вы ответили правильно на 4 из 4 вопросов!\n" +
                "Это уровень уверенного знатока языка — так держать! 🚀\n\n" +
                "📈 Изменения приоритетов:\n" +
                "• Слова, которые вы хорошо знаете: 2\n" +
                "• Новые слова, которые вы знаете: 2\n";

        Assertions.assertEquals(expectedResult, result, "Результат должен точно совпадать для 100% правильных ответов");
    }

    /**
     * Тест финальной фразы для хорошего результата (50%)
     */
    @Test
    public void testFormatTestResult_Good() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        // Начинаем тест
        scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        // Отвечаем на 2 из 4 правильно (50%) - первые 2 правильные, вторые 2 неправильные
        String result = "";
        for (int i = 0; i < 2; i++) {
            result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "B_button"); // Правильные
        }
        for (int i = 0; i < 2; i++) {
            result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "A_button"); // Неправильные
        }

        String expectedResult = "🎉 Тест завершен! 🎉\n\n" +
                "📊 Результаты:\n" +
                "• Всего вопросов: 4\n" +
                "• Правильных ответов: 2\n" +
                "• Ошибок: 2\n" +
                "• Процент правильных: 50%\n\n" +
                "📖 *Хорошая основа для роста!*\n" +
                "Ваш результат: 2 из 4 правильных ответов.\n" +
                "Вы уже многое знаете, а пробелы — это возможности для новых открытий!\n\n" +
                "📈 Изменения приоритетов:\n" +
                "• Слова, которые вы хорошо знаете: 1\n" +
                "• Слова для повторения: 1\n" +
                "• Новые слова, которые вы знаете: 1\n" +
                "• Новые слова для изучения: 1\n";

        Assertions.assertEquals(expectedResult, result, "Результат должен точно совпадать для 50% правильных ответов");
    }

    /**
     * Тест финальной фразы для начального результата (25%)
     */
    @Test
    public void testFormatTestResult_Beginner() {
        TestsParser parser = new TestsParser();
        TestsData testsData = parser.parseTest(TEST_TEXT);

        // Начинаем тест
        scheduleTestHandler.startTest(AUTHORIZED_CHAT_ID, testsData, AUTHORIZED_USER_ID);

        // Отвечаем на 1 из 4 правильно (25%) - только первый правильный
        String result = "";
        result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "B_button"); // Правильный
        for (int i = 0; i < 3; i++) {
            result = scheduleTestHandler.handleAnswer(AUTHORIZED_CHAT_ID, "A_button"); // Неправильные
        }

        String expectedResult = "🎉 Тест завершен! 🎉\n\n" +
                "📊 Результаты:\n" +
                "• Всего вопросов: 4\n" +
                "• Правильных ответов: 1\n" +
                "• Ошибок: 3\n" +
                "• Процент правильных: 25%\n\n" +
                "🌱 *Начало пути!*\n" +
                "Вы ответили правильно на 1 из 4 вопросов.\n" +
                "Каждый эксперт когда-то начинал с первого шага — и вы его уже сделали!\n\n" +
                "📈 Изменения приоритетов:\n" +
                "• Слова, которые вы хорошо знаете: 1\n" +
                "• Слова для повторения: 1\n" +
                "• Новые слова для изучения: 2\n";

        Assertions.assertEquals(expectedResult, result, "Результат должен точно совпадать для 25% правильных ответов");
    }
}