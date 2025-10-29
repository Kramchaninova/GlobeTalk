package org.example;

import java.io.IOException;

/**
 * SpeedTestYesButton - класс, который хранит запрос (PROMPT) для теста на скорость,
 * подгружает API токен OpenRouter и отправляет запрос для генерации теста.
 */
public class SpeedTestYesButton {

    // Наш новый промпт на 12 вопросов
    private static final String PROMPT = """
            Сгенерируй тест на 12 вопросов на проверку уровня английского языка:
            - 4 вопроса уровня A1-A2 (по 1 баллу каждый)
            - 4 вопроса уровня B1-B2 (по 2 балла каждый)
            - 4 вопроса уровня C1-C2 (по 3 балла каждый)

            ФОРМАТ КАЖДОГО ВОПРОСА (ОБЯЗАТЕЛЬНО СОБЛЮДАТЬ):
            1 (1)
            Текст вопроса или задания
            A. Вариант ответа A
            B. Вариант ответа B
            C. Вариант ответа C
            D. Вариант ответа D
            Answer: A

            ВАЖНЫЕ ПРАВИЛА:
            1. Номер вопроса и баллы в скобках БЕЗ слова 'points' или 'баллов'
            2. После номера и баллов - сразу текст вопроса
            3. Варианты ответов начинаются с A. B. C. D.
            4. В конце каждого вопроса строка 'Answer: X' где X - правильная буква
            5. Вопросы должны быть разнообразными: грамматика, лексика, понимание
            6. Расположи вопросы в случайном порядке сложности
            """;

    /**
     * Метод для генерации теста через OpenRouter
     */
    public String generateTest() {
        try {
            // Загружаем API ключ через TokenOpenRouter
            TokenOpenRouter tokenOpenRouter = new TokenOpenRouter();
            tokenOpenRouter.load();

            String apiKey = tokenOpenRouter.get();

            if (apiKey == null || apiKey.isEmpty()) {
                return "API ключ не найден";
            }

            // Создаём клиент OpenRouter и отправляем запрос
            OpenRouterClient openRouterClient = new OpenRouterClient(apiKey);
            return openRouterClient.sendRequest(PROMPT);

        } catch (IOException | InterruptedException e) {
            return "Ошибка при генерации теста: " + e.getMessage();
        }
    }
}
