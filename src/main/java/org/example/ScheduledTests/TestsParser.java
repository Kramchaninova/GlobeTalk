package org.example.ScheduledTests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер тестов из текста от гпт
 * Извлекает вопросы, варианты ответов и метаданные для создания тестов
 */
public class TestsParser {

    /**
     * Парсит текст теста и возвращает структурированные данные
     * @param testText сырой текст теста от AI
     * @return объект TestsData с распарсенными вопросами
     */
    public TestsData parseTest(String testText) {
        TestsData testsData = new TestsData();

        System.out.println("\n\n        TestsParser.parseTest()");
        System.out.println("Исходный текст теста:");
        System.out.println(testText);

        try {
            // Разделяем текст на блоки вопросов
            String[] questionBlocks = testText.split("(?=Вопрос\\s*\\d*:|Вопрос:)");

            int parsedQuestions = 0;
            for (int i = 0; i < questionBlocks.length; i++) {
                String block = questionBlocks[i].trim();

                // Пропускаем заголовки и пустые блоки
                if (block.isEmpty() ||
                        block.startsWith("ТЕСТ НА ЗНАНИЕ") ||
                        block.startsWith("Проверьте свой") ||
                        block.length() < 20) {
                    continue;
                }

                TestsData.QuestionData question = parseQuestion(block, parsedQuestions);
                if (question != null) {
                    testsData.addQuestion(question);
                    parsedQuestions++;
                    System.out.println("✅ Вопрос распарсен: '" + question.getEnglishWord() + "' -> '" + question.getTranslation() + "'");
                } else {
                    System.out.println("❌ Не удалось распарсить блок " + i);
                }
            }

        } catch (Exception e) {
            System.out.println("Ошибка парсинга теста: " + e.getMessage());
            e.printStackTrace();
        }

        return testsData;
    }

    /**
     * Парсит отдельный блок вопроса
     * @param block текстовый блок с вопросом
     * @param questionNumber номер вопроса (для логирования)
     * @return распарсенные данные вопроса или null при ошибке
     */
    private TestsData.QuestionData parseQuestion(String block, int questionNumber) {
        try {

            // Паттерны для извлечения данных
            Pattern wordPattern = Pattern.compile("Слово:\\s*([^\\-\n\r]+)\\s*-\\s*([^\n\r]+)", Pattern.CASE_INSENSITIVE);
            Pattern typePattern = Pattern.compile("Тип:\\s*(ПРИОРИТЕТНОЕ|НОВОЕ)", Pattern.CASE_INSENSITIVE);
            Pattern correctPattern = Pattern.compile("Ответ:\\s*([A-D])", Pattern.CASE_INSENSITIVE);

            // Извлекаем данные из блока
            String englishWord = null;
            String translation = null;
            String wordType = "НОВОЕ";
            String correctAnswer = "A";

            Matcher wordMatcher = wordPattern.matcher(block);
            Matcher typeMatcher = typePattern.matcher(block);
            Matcher correctMatcher = correctPattern.matcher(block);

            // Извлекаем слово и перевод
            if (wordMatcher.find()) {
                englishWord = wordMatcher.group(1).trim();
                translation = wordMatcher.group(2).trim();
            } else {
                // Альтернативный поиск с другим паттерном
                Pattern altWordPattern = Pattern.compile("Слов[ао]:?\\s*([^\\-\n\r]+)\\s*-\\s*([^\n\r]+)", Pattern.CASE_INSENSITIVE);
                Matcher altWordMatcher = altWordPattern.matcher(block);
                if (altWordMatcher.find()) {
                    englishWord = altWordMatcher.group(1).trim();
                    translation = altWordMatcher.group(2).trim();
                } else {
                    System.out.println("❌ Не удалось извлечь слово и перевод");
                    return null;
                }
            }

            // Извлекаем тип вопроса
            if (typeMatcher.find()) {
                wordType = typeMatcher.group(1);
                System.out.println("Найдено тип: " + wordType);
            }

            // Извлекаем правильный ответ
            if (correctMatcher.find()) {
                correctAnswer = correctMatcher.group(1);
                System.out.println("Найдено правильный ответ: " + correctAnswer);
            }

            // Формируем текст вопроса
            String questionText = buildQuestionText(block, englishWord);

            // Валидация данных
            if (englishWord == null || englishWord.trim().isEmpty() || englishWord.equals("Н/Д") ||
                    translation == null || translation.trim().isEmpty()) {
                System.out.println("❌ Пустое или невалидное слово/перевод");
                return null;
            }

            return new TestsData.QuestionData(
                    questionText, correctAnswer, wordType, englishWord, translation
            );

        } catch (Exception e) {
            System.out.println("❌ Ошибка парсинга вопроса " + (questionNumber + 1) + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Формирует полный текст вопроса с вариантами ответов
     * @param block исходный блок текста
     * @param englishWord английское слово
     * @return отформатированный текст вопроса
     */
    private String buildQuestionText(String block, String englishWord) {
        try {
            StringBuilder sb = new StringBuilder();
            String[] lines = block.split("\n");

            boolean inQuestion = false;
            boolean foundOptions = false;
            int optionsCount = 0;
            StringBuilder questionBody = new StringBuilder();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Пропускаем технические строки
                if (line.startsWith("Ответ:") || line.startsWith("Тип:") || line.startsWith("Слово:") || line.startsWith("Слово")) {
                    continue;
                }

                // Начинаем вопрос с любой значимой строки
                if (!inQuestion && !line.matches("[A-D]\\)\\s*.+")) {
                    inQuestion = true;
                }

                if (inQuestion) {
                    // Добавляем варианты ответов
                    if (line.matches("[A-D]\\)\\s*.+")) {
                        if (questionBody.length() > 0 && !foundOptions) {
                            sb.append(questionBody.toString()).append("\n");
                            questionBody.setLength(0);
                        }
                        sb.append(line).append("\n");
                        optionsCount++;
                        foundOptions = true;
                    }
                    // Добавляем текст вопроса
                    else if (!foundOptions) {
                        questionBody.append(line).append("\n");
                    }
                }

                if (optionsCount >= 4) break;
            }

            // Добавляем заголовок по умолчанию если вопрос пустой
            if (sb.length() == 0 && questionBody.length() == 0) {
                sb.append("Выберите правильный перевод слова \"").append(englishWord).append("\"\n");
            } else if (questionBody.length() > 0 && !foundOptions) {
                sb.insert(0, questionBody.toString());
            }

            String result = sb.toString().trim();

            // Добавляем шаблонные варианты если не нашли
            if (!foundOptions || optionsCount < 4) {
                result += "\nA) вариант перевода 1\nB) вариант перевода 2\nC) вариант перевода 3\nD) вариант перевода 4";
            }

            return result;

        } catch (Exception e) {
            System.out.println("Ошибка формирования текста вопроса: " + e.getMessage());
            return "Выберите правильный перевод для слова: " + englishWord +
                    "\nA) вариант перевода 1\nB) вариант перевода 2\nC) вариант перевода 3\nD) вариант перевода 4";
        }
    }
}