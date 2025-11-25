package org.example.ScheduledTests;

import org.example.OpenRouter.OpenRouterClient;
import org.example.Tokens.TokenOpenRouter;

import java.io.IOException;
import java.util.List;

/**
 * Хранит запрос (PROMT),
 * подгружает api token openrouter,
 * вызывает метод отправки нашего запроса непосредственно ии
 */
public class ScheduleGenerateTests {
    private final ScheduleTests scheduleTests;

    public ScheduleGenerateTests(ScheduleTests scheduleTests) {
        this.scheduleTests = scheduleTests;
    }

    /**
     * Метод для генерации теста
     */
    public String generateTest(long userId) {
        try {
            // Создаем динамический промпт
            String dynamicPrompt = createPrompt(userId);

            // загружаем API ключ через Token_Openrouter
            TokenOpenRouter tokenOpenRouter = new TokenOpenRouter();
            tokenOpenRouter.load();

            String apiKey = tokenOpenRouter.get();

            if (apiKey == null || apiKey.isEmpty()) {
                return "API ключ не найден";
            }

            // создаем экземпляр класса OpenRouterClient
            OpenRouterClient openRouterClient = new OpenRouterClient(apiKey);
            return openRouterClient.sendRequest(dynamicPrompt);

        } catch (IOException | InterruptedException e) {
            return "ошибка при генерации теста: " + e.getMessage();
        }
    }

    /**
     * Создает промпт с автоматической подстановкой приоритетных слов
     */
    private String createPrompt(long userId) {
        // Получаем приоритетные слова для пользователя
        List<String[]> priorityWords = scheduleTests.getWordsWithMaxPriority(userId);

        // Формируем строку с приоритетных слов для промпта
        String priorityWordsString = formatPriorityWordsForPrompt(priorityWords);

        return "Создай тест по английскому языку для изучения слов. \n" +
                "\n" +
                "ТЕХНИЧЕСКИЕ ТРЕБОВАНИЯ:\n" +
                "- Тест должен содержать ровно 10 вопросов\n" +
                "- Обязательно должны быть вопросы на слова из этого списка: " + priorityWordsString + "\n" +
                "- Остальные вопроса должны быть на новые случайные английские слова\n" +
                "- Каждый вопрос должен иметь 4 варианта ответа (A, B, C, D)\n" +
                "- Только один вариант ответа правильный\n" +
                "- Вопросы должны быть разнообразными: выбор перевода, выбор слова по описанию, заполнение пропусков в предложениях\n" +
                "\n" +
                "ВАЖНО! ФОРМАТ ВЫВОДА - НАЧИНАЙ СРАЗУ С ПЕРВОГО ВОПРОСА:\n" +
                "- НЕ добавляй заголовки типа \"ТЕСТ НА ЗНАНИЕ АНГЛИЙСКИХ СЛОВ\"\n" +
                "- НЕ добавляй вступительные фразы типа \"Проверьте свой словарный запас\"\n" +
                "- Начинай вывод сразу с первого вопроса\n" +
                "\n" +
                "СТРОГИЙ ФОРМАТ КАЖДОГО ВОПРОСА:\n" +
                "\n" +
                "Вопрос:\n" +
                "[Текст вопроса]\n" +
                "\n" +
                "A) [Вариант 1]\n" +
                "B) [Вариант 2] \n" +
                "C) [Вариант 3]\n" +
                "D) [Вариант 4]\n" +
                "\n" +
                "Ответ: [Правильная буква]\n" +
                "Тип: ПРИОРИТЕТНОЕ\n" +
                "Слово: [Слово из списка] - [Перевод]\n" +
                "\n" +
                "Вопрос:\n" +
                "[Текст вопроса]\n" +
                "\n" +
                "A) [Вариант 1]\n" +
                "B) [Вариант 2]\n" +
                "C) [Вариант 3] \n" +
                "D) [Вариант 4]\n" +
                "\n" +
                "Ответ: [Правильная буква]\n" +
                "Тип: НОВОЕ\n" +
                "Слово: [Новое слово] - [Перевод]\n" +
                "\n" +
                "... и так для всех 10 вопросов\n" +
                "\n" +
                "ОСОБЫЕ УКАЗАНИЯ:\n" +
                "\n" +
                "ДЛЯ 6 ВОПРОСОВ НА ПРИОРИТЕТНЫЕ СЛОВА:\n" +
                "- Использовать слова из предоставленного списка: " + priorityWordsString + "\n" +
                "- В конце вопроса использовать подпись \"Тип: ПРИОРИТЕТНОЕ\"\n" +
                "- Указывать слово из приоритетного списка и его перевод\n" +
                "\n" +
                "ДЛЯ 4 ВОПРОСОВ НА НОВЫЕ СЛОВА:\n" +
                "- Использовать случайные новые английские слова\n" +
                "- В конце вопроса использовать подпись \"Тип: НОВОЕ\"\n" +
                "- Указывать новое слово и его перевод\n" +
                "\n" +
                "ПРИМЕР КОРРЕКТНОГО ФОРМАТА (начинай сразу так):\n" +
                "\n" +
                "Вопрос:\n" +
                "Выберите правильный перевод слова \"accomplishment\"\n" +
                "\n" +
                "A) неудача\n" +
                "B) достижение  \n" +
                "C) начало\n" +
                "D) препятствие\n" +
                "\n" +
                "Ответ: B\n" +
                "Тип: ПРИОРИТЕТНОЕ\n" +
                "Слово: accomplishment - достижение\n" +
                "\n" +
                "Вопрос:\n" +
                "Какое слово означает \"устойчивый, стабильный\"?\n" +
                "\n" +
                "A) fragile\n" +
                "B) stable\n" +
                "C) temporary  \n" +
                "D) flexible\n" +
                "\n" +
                "Ответ: B\n" +
                "Тип: НОВОЕ\n" +
                "Слово: stable - устойчивый, стабильный\n" +
                "\n" +
                "ТИПЫ ВОПРОСОВ:\n" +
                "1. \"Выберите правильный перевод слова: [слово]\"\n" +
                "2. \"Какое слово соответствует описанию: [описание на русском]\"\n" +
                "3. \"Выберите слово, которое подходит в предложение: [предложение с пропуском]\"\n" +
                "4. \"Найдите синоним к слову: [слово]\"\n" +
                "5. \"Выберите антоним к слову: [слово]\"\n" +
                "\n" +
                "ТРЕБОВАНИЯ К СОДЕРЖАНИЮ:\n" +
                "- Новые слова должны быть полезными для повседневного общения\n" +
                "- Сложность должна соответствовать intermediate уровню\n" +
                "- Варианты ответов должны быть правдоподобными, но не очевидными\n" +
                "- Вопросы должны проверять реальное понимание слов, а не просто запоминание\n" +
                "\n" +
                "КОНЕЧНЫЙ ФОРМАТ:\n" +
                "- Начинай сразу с \"Вопрос:\"\n" +
                "- Закончи после 10-го вопроса\n" +
                "- Без заголовков, без вступлений, без заключений\n" +
                "\n" +
                "Генерируй тест на русском языке, сохраняя английские слова в оригинале.";
    }

    /**
     * Форматирует приоритетные слова для вставки в промпт
     */
    private String formatPriorityWordsForPrompt(List<String[]> priorityWords) {
        if (priorityWords.isEmpty()) {
            return "список пуст";
        }

        StringBuilder sb = new StringBuilder();
        for (String[] wordPair : priorityWords) {
            sb.append(wordPair[0]).append(" - ").append(wordPair[1]).append(", ");
        }
        // Убираем последнюю запятую и пробел
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }
}