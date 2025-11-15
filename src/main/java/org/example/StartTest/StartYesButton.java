package org.example.StartTest;

import org.example.OpenRouter.OpenRouterClient;
import org.example.Tokens.TokenOpenRouter;

import java.io.IOException;

/** StartYesButton - класс, который хранит запрос (PROMT),
 * подгружает api token openrouter,
 * вызывает метод отправки нашего запроса непосредственно ии
 */


public class StartYesButton {
    //наш запрос
    private static final String PROMPT= "Сгенерируй тест на 9 вопросов на проверку уровня английского языка:\n" +
            "- 3 вопроса уровня A1-A2 (по 1 баллу каждый)\n" +
            "- 3 вопроса уровня B1-B2 (по 2 балла каждый)\n" +
            "- 3 вопроса уровня C1-C2 (по 3 балла каждый)\n\n" +

            "ФОРМАТ КАЖДОГО ВОПРОСА (ОБЯЗАТЕЛЬНО СОБЛЮДАТЬ):\n" +
            "1 (1)\n" +
            "Текст вопроса или задания\n" +
            "A. Вариант ответа A\n" +
            "B. Вариант ответа B\n" +
            "C. Вариант ответа C\n" +
            "D. Вариант ответа D\n" +
            "Answer: A\n\n" +

            "ВАЖНЫЕ ПРАВИЛА:\n" +
            "1. Номер вопроса и баллы в скобках БЕЗ слова 'points' или 'баллов'\n" +
            "2. После номера и баллов - сразу текст вопроса\n" +
            "3. Варианты ответов начинаются с A. B. C. D.\n" +
            "4. В конце каждого вопроса строка 'Answer: X' где X - правильная буква\n" +
            "5. Вопросы должны быть разнообразными: грамматика, лексика, понимание\n" +
            "6. Расположи вопросы в случайном порядке сложности";

    /**
     * Метод для генерации теста и сохранение в txt файл
     */
    public String generateTest() {
        try {
            // загружаем API ключ через Token_Openrouter, ОЧЕНЬ ВАЖНО: мы работаем не напрямую с гпт
            //Опен Роутер - это "лпатформа-посредник"
            TokenOpenRouter tokenOpenRouter = new TokenOpenRouter();
            tokenOpenRouter.load();

            String apiKey = tokenOpenRouter.get();

            //оносительно бесполезная вещь
            if (apiKey == null || apiKey.isEmpty()) {
                return "API ключ не найден";
            }

            //создаем экземпляр класса OpenRouterClient и закидываем тут наш токен опенроутера
            OpenRouterClient openRouterClient = new OpenRouterClient(apiKey);
            return openRouterClient.sendRequest(PROMPT);

        } catch (IOException | InterruptedException e) {
            return "ошибка при генерации теста: " + e.getMessage();
        }
    }
}
