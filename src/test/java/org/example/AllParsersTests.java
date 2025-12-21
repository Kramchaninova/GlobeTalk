package org.example;

import org.example.ScheduledNewWord.MessageParser;
import org.example.ScheduledNewWord.WordData;
import org.example.ScheduledOldWord.OldWordData;
import org.example.ScheduledOldWord.OldWordParser;
import org.example.ScheduledTests.TestsData;
import org.example.ScheduledTests.TestsParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * AllParsersTests - тестирует все парсеры в системе
 * Тестируемые парсеры:
 * - MessageParser (ScheduledNewWord) - парсит структурированные данные слов для ежедневных слов
 * - OldWordParser (ScheduledOldWord) - парсит тесты для слов с низким приоритетом
 * - TestsParser (ScheduledTests) - парсит комплексные тесты с множеством вопросов
 */
public class AllParsersTests {

    /**
     * Тест парсера MessageParser с валидными входными данными
     * Проверяет корректное извлечение всех полей структурированного слова
     */
    @Test
    public void testMessageParser_ValidInput() {
        MessageParser parser = new MessageParser();

        String input = "WORD: accomplishment\n" +
                "TRANSLATION: достижение\n" +
                "LEVEL: B2\n" +
                "PART_OF_SPEECH: noun\n" +
                "EXAMPLE: Finishing the project was a great accomplishment\n" +
                "EXAMPLE_TRANSLATION: Завершение проекта было большим достижением\n" +
                "RELATED_WORDS: achievement, success, completion\n" +
                "TOPIC: work & career";

        WordData result = parser.parseWord(input);

        Assertions.assertEquals("accomplishment", result.getWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("достижение", result.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("B2", result.getLevel(), "Уровень должен совпадать");
        Assertions.assertEquals("noun", result.getPartOfSpeech(), "Часть речи должна совпадать");
        Assertions.assertEquals("Finishing the project was a great accomplishment", result.getExample(), "Пример должен совпадать");
        Assertions.assertEquals("Завершение проекта было большим достижением", result.getExampleTranslation(), "Перевод примера должен совпадать");
        Assertions.assertEquals(3, result.getRelatedWords().size(), "Должно быть 3 связанных слова");
        Assertions.assertEquals("work & career", result.getTopic(), "Тема должна совпадать");
    }

    /**
     * Тест парсера MessageParser с отсутствующими обязательными полями
     * Проверяет обработку неполных данных
     */
    @Test
    public void testMessageParser_MissingRequiredFields() {
        MessageParser parser = new MessageParser();

        String input = "LEVEL: B2\n" +
                "PART_OF_SPEECH: noun\n" +
                "EXAMPLE: Some example";

        WordData result = parser.parseWord(input);

        Assertions.assertNull(result, "Результат должен быть null при отсутствии обязательных полей");
    }

    /**
     * Тест парсера MessageParser с пустым входом
     * Проверяет обработку пустых данных
     */
    @Test
    public void testMessageParser_EmptyInput() {
        MessageParser parser = new MessageParser();

        WordData result = parser.parseWord("");

        Assertions.assertNull(result, "Результат должен быть null для пустого ввода");
    }

    /**
     * Тест парсера MessageParser без связанных слов
     * Проверяет обработку отсутствия опциональных полей
     */
    @Test
    public void testMessageParser_NoRelatedWords() {
        MessageParser parser = new MessageParser();

        String input = "WORD: test\n" +
                "TRANSLATION: тест\n" +
                "LEVEL: A1\n" +
                "PART_OF_SPEECH: noun\n" +
                "EXAMPLE: This is a test\n" +
                "EXAMPLE_TRANSLATION: Это тест\n" +
                "TOPIC: education";

        WordData result = parser.parseWord(input);

        Assertions.assertEquals("test", result.getWord(), "Английское слово должно совпадать");
        Assertions.assertEquals(0, result.getRelatedWords().size(), "Список связанных слов должен быть пустым");
    }

    /**
     * Тест парсера OldWordParser с валидными входными данными
     * Проверяет извлечение вопроса и правильного ответа
     */
    @Test
    public void testOldWordParser_ValidInput() {
        OldWordParser parser = new OldWordParser();

        String testText = "Вопрос:\n" +
                "Какое слово означает \"достижение\"?\n\n" +
                "A) failure\n" +
                "B) accomplishment\n" +
                "C) beginning\n" +
                "D) obstacle\n\n" +
                "Ответ: B";

        OldWordData result = parser.parseTest(testText, "accomplishment", "достижение");

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals("accomplishment", result.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("достижение", result.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("B", result.getCorrectAnswer(), "Правильный ответ должен быть 'B'");
    }

    /**
     * Тест парсера OldWordParser без секции ответа
     * Проверяет обработку неполного формата
     */
    @Test
    public void testOldWordParser_NoAnswerSection() {
        OldWordParser parser = new OldWordParser();

        String testText = "Вопрос:\n" +
                "Какое слово означает \"достижение\"?\n\n" +
                "A) failure\n" +
                "B) accomplishment\n" +
                "C) beginning\n" +
                "D) obstacle";

        // Ожидаем исключение с конкретным сообщением
        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            parser.parseTest(testText, "accomplishment", "достижение");
        });

        Assertions.assertTrue(exception.getMessage().contains("некорректный формат") ||
                        exception.getCause().getMessage().contains("некорректный формат"),
                "Должно содержать сообщение о некорректном формате");
    }

    /**
     * Тест парсера OldWordParser с минимальными данными
     * Проверяет обработку минимального валидного формата
     */
    @Test
    public void testOldWordParser_MinimalInput() {
        OldWordParser parser = new OldWordParser();

        String testText = "Простой вопрос\n\n" +
                "A) вариант1\n" +
                "B) вариант2\n\n" +
                "Ответ: A";

        OldWordData result = parser.parseTest(testText, "test", "тест");

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals("test", result.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("тест", result.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("A", result.getCorrectAnswer(), "Правильный ответ должен быть 'A'");
    }

    /**
     * Тест парсера OldWordParser с альтернативным форматом
     */
    @Test
    public void testOldWordParser_AlternativeFormat() {
        OldWordParser parser = new OldWordParser();

        String testText = "Выберите правильный перевод слова \"resilient\":\n\n" +
                "A) хрупкий\n" +
                "B) устойчивый\n" +
                "C) временный\n" +
                "D) гибкий\n\n" +
                "Ответ: B";

        OldWordData result = parser.parseTest(testText, "resilient", "устойчивый");

        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals("resilient", result.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("устойчивый", result.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("B", result.getCorrectAnswer(), "Правильный ответ должен быть 'B'");
    }

    /**
     * Тест парсера OldWordParser с неправильным форматом ответа
     */
    @Test
    public void testOldWordParser_InvalidAnswerFormat() {
        OldWordParser parser = new OldWordParser();

        String testText = "Вопрос:\n" +
                "Тестовый вопрос\n\n" +
                "A) вариант1\n" +
                "B) вариант2\n\n" +
                "Ответ: неправильный_формат";

        OldWordData result = parser.parseTest(testText, "test", "тест");

        // Если парсер принимает любой формат, проверяем что данные корректны
        Assertions.assertNotNull(result, "Результат не должен быть null");
        Assertions.assertEquals("test", result.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("тест", result.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("НЕПРАВИЛЬНЫЙ_ФОРМАТ", result.getCorrectAnswer(), "Должен сохранить ответ как есть");
    }

    /**
     * Тест парсера TestsParser с одним валидным вопросом
     * Проверяет извлечение структурированных данных вопроса
     */
    @Test
    public void testTestsParser_SingleQuestion() {
        TestsParser parser = new TestsParser();

        String testText = "Вопрос:\n" +
                "Выберите правильный перевод слова \"accomplishment\"\n\n" +
                "A) неудача\n" +
                "B) достижение\n" +
                "C) начало\n" +
                "D) препятствие\n\n" +
                "Ответ: B\n" +
                "Тип: ПРИОРИТЕТНОЕ\n" +
                "Слово: accomplishment - достижение";

        TestsData result = parser.parseTest(testText);

        Assertions.assertEquals(1, result.getQuestions().size(), "Должен быть распарсен 1 вопрос");

        TestsData.QuestionData question = result.getQuestions().get(0);
        Assertions.assertEquals("accomplishment", question.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("достижение", question.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("B", question.getCorrectAnswer(), "Правильный ответ должен быть 'B'");
        Assertions.assertEquals("ПРИОРИТЕТНОЕ", question.getWordType(), "Тип слова должен быть 'ПРИОРИТЕТНОЕ'");
    }

    /**
     * Тест парсера TestsParser с несколькими вопросами
     * Проверяет обработку множественных вопросов в одном тесте
     */
    @Test
    public void testTestsParser_MultipleQuestions() {
        TestsParser parser = new TestsParser();

        String testText = "Вопрос:\n" +
                "Выберите правильный перевод слова \"stable\"\n\n" +
                "A) хрупкий\n" +
                "B) устойчивый\n" +
                "C) временный\n" +
                "D) гибкий\n\n" +
                "Ответ: B\n" +
                "Тип: НОВОЕ\n" +
                "Слово: stable - устойчивый\n\n" +
                "Вопрос:\n" +
                "Какое слово означает \"начало\"?\n\n" +
                "A) end\n" +
                "B) beginning\n" +
                "C) middle\n" +
                "D) continuation\n\n" +
                "Ответ: B\n" +
                "Тип: ПРИОРИТЕТНОЕ\n" +
                "Слово: beginning - начало";

        TestsData result = parser.parseTest(testText);

        Assertions.assertEquals(2, result.getQuestions().size(), "Должно быть распарсено 2 вопроса");

        TestsData.QuestionData firstQuestion = result.getQuestions().get(0);
        Assertions.assertEquals("stable", firstQuestion.getEnglishWord(), "Английское слово первого вопроса должно совпадать");
        Assertions.assertEquals("устойчивый", firstQuestion.getTranslation(), "Перевод первого вопроса должен совпадать");
        Assertions.assertEquals("НОВОЕ", firstQuestion.getWordType(), "Тип первого вопроса должен быть 'НОВОЕ'");

        TestsData.QuestionData secondQuestion = result.getQuestions().get(1);
        Assertions.assertEquals("beginning", secondQuestion.getEnglishWord(), "Английское слово второго вопроса должно совпадать");
        Assertions.assertEquals("начало", secondQuestion.getTranslation(), "Перевод второго вопроса должен совпадать");
        Assertions.assertEquals("ПРИОРИТЕТНОЕ", secondQuestion.getWordType(), "Тип второго вопроса должен быть 'ПРИОРИТЕТНОЕ'");
    }

    /**
     * Тест парсера TestsParser с неполными данными вопроса
     * Проверяет обработку вопросов с отсутствующими полями
     * Он и должен иметь ошибку
     */
    @Test
    public void testTestsParser_IncompleteQuestion() {
        TestsParser parser = new TestsParser();

        String testText = "Вопрос:\n" +
                "Неполный вопрос без всех полей\n\n" +
                "A) вариант1\n" +
                "B) вариант2\n\n" +
                "Ответ: A";

        TestsData result = parser.parseTest(testText);

        Assertions.assertEquals(0, result.getQuestions().size(), "Неполные вопросы должны быть пропущены");
    }

    /**
     * Тест парсера TestsParser с пустым входом
     * Проверяет обработку пустого теста
     */
    @Test
    public void testTestsParser_EmptyInput() {
        TestsParser parser = new TestsParser();

        String testText = "";

        TestsData result = parser.parseTest(testText);

        Assertions.assertEquals(0, result.getQuestions().size(), "Для пустого ввода не должно быть вопросов");
    }

    /**
     * Тест парсера TestsParser с альтернативным форматом слова
     * Проверяет обработку различных вариантов форматирования
     */
    @Test
    public void testTestsParser_AlternativeWordFormat() {
        TestsParser parser = new TestsParser();

        String testText = "Вопрос:\n" +
                "Тестовый вопрос\n\n" +
                "A) вариант1\n" +
                "B) вариант2\n" +
                "C) вариант3\n" +
                "D) вариант4\n\n" +
                "Ответ: C\n" +
                "Тип: ПРИОРИТЕТНОЕ\n" +
                "Слово: alternative - альтернатива";

        TestsData result = parser.parseTest(testText);

        Assertions.assertEquals(1, result.getQuestions().size(), "Должен быть распарсен 1 вопрос");

        TestsData.QuestionData question = result.getQuestions().get(0);
        Assertions.assertEquals("alternative", question.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("альтернатива", question.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("C", question.getCorrectAnswer(), "Правильный ответ должен быть 'C'");
        Assertions.assertEquals("ПРИОРИТЕТНОЕ", question.getWordType(), "Тип слова должен быть 'ПРИОРИТЕТНОЕ'");
    }

    /**
     * Тест парсера TestsParser с вопросами без типа слова
     */
    @Test
    public void testTestsParser_QuestionsWithoutWordType() {
        TestsParser parser = new TestsParser();

        String testText = "Вопрос:\n" +
                "Вопрос без типа слова\n\n" +
                "A) вариант1\n" +
                "B) вариант2\n\n" +
                "Ответ: A\n" +
                "Слово: test - тест";

        TestsData result = parser.parseTest(testText);

        // Проверяем что результат не null
        Assertions.assertNotNull(result, "Результат не должен быть null");

        // Проверяем количество распарсенных вопросов
        Assertions.assertEquals(1, result.getQuestions().size(), "Должен быть распарсен 1 вопрос");

        // Проверяем конкретные данные вопроса
        TestsData.QuestionData question = result.getQuestions().get(0);

        // Проверяем английское слово и перевод
        Assertions.assertEquals("test", question.getEnglishWord(), "Английское слово должно быть 'test'");
        Assertions.assertEquals("тест", question.getTranslation(), "Перевод должен быть 'тест'");

        // Проверяем правильный ответ
        Assertions.assertEquals("A", question.getCorrectAnswer(), "Правильный ответ должен быть 'A'");

        // Проверяем тип слова (по умолчанию должен быть "НОВОЕ")
        Assertions.assertEquals("НОВОЕ", question.getWordType(), "Тип слова по умолчанию должен быть 'НОВОЕ'");

        // Проверяем что текст вопроса содержит ожидаемые элементы
        String questionText = question.getQuestionText();
        Assertions.assertTrue(questionText.contains("A)") && questionText.contains("B)") &&
                        questionText.contains("C)") && questionText.contains("D)"),
                "Текст вопроса должен содержать все варианты ответов A-D");
    }

    /**
     * Тест парсера TestsParser с различными вариантами ответов
     */
    @Test
    public void testTestsParser_VariousAnswerOptions() {
        TestsParser parser = new TestsParser();

        String testText = "Вопрос:\n" +
                "Вопрос с разными вариантами ответов\n\n" +
                "A) первый вариант\n" +
                "B) второй вариант\n" +
                "C) третий вариант\n" +
                "D) четвертый вариант\n\n" +
                "Ответ: D\n" +
                "Тип: НОВОЕ\n" +
                "Слово: various - различный";

        TestsData result = parser.parseTest(testText);

        Assertions.assertEquals(1, result.getQuestions().size(), "Должен быть распарсен 1 вопрос");

        TestsData.QuestionData question = result.getQuestions().get(0);
        Assertions.assertEquals("various", question.getEnglishWord(), "Английское слово должно совпадать");
        Assertions.assertEquals("различный", question.getTranslation(), "Перевод должен совпадать");
        Assertions.assertEquals("D", question.getCorrectAnswer(), "Правильный ответ должен быть 'D'");
        Assertions.assertEquals("НОВОЕ", question.getWordType(), "Тип слова должен быть 'НОВОЕ'");
    }
}