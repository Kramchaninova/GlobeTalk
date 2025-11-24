package org.example.ScheduledOldWord;

import org.example.OpenRouter.OpenRouterClient;
import org.example.Tokens.TokenOpenRouter;

import java.io.IOException;

/**
 * Генератор тестов для слов с низким приоритетом
 * Создает тест с 4 вариантами ответа по одному слову
 */
public class OldWordGenerator {
    private final OldWord wordProcessor;

    public OldWordGenerator(OldWord wordProcessor) {
        this.wordProcessor = wordProcessor;
    }

    /**
     * Генерирует тест по слову с низким приоритетом
     */
    public String generateTest(String englishWord, String translation) {
        try {
            // Создаем динамический промпт
            String dynamicPrompt = createPrompt(englishWord, translation);

            // Загружаем API ключ через Token_Openrouter
            TokenOpenRouter tokenOpenRouter = new TokenOpenRouter();
            tokenOpenRouter.load();

            String apiKey = tokenOpenRouter.get();

            if (apiKey == null || apiKey.isEmpty()) {
                return "❌ API ключ не найден";
            }

            // Создаем экземпляр класса OpenRouterClient
            OpenRouterClient openRouterClient = new OpenRouterClient(apiKey);
            return openRouterClient.sendRequest(dynamicPrompt);

        } catch (IOException | InterruptedException e) {
            return "❌ Ошибка при генерации теста: " + e.getMessage();
        }
    }

    /**
     * Создает промпт для генерации теста по одному слову
     */
    private String createPrompt(String englishWord, String translation) {
        return "Создай один вопрос теста на слово: " + englishWord + " - " + translation + "\n\n" +

                "ТРЕБОВАНИЯ:\n" +
                "- 1 вопрос с 4 вариантами ответа (A,B,C,D)\n" +
                "- Только один правильный ответ\n" +
                "- Правильный ответ должен быть связан со словом на английский: " + englishWord + "\n" +
                "- Неправильные варианты - правдоподобные\n" +
                "- Вопрос и варианты ответов - единое целое\n\n" +

                "ФОРМАТ (начинай сразу):\n" +
                "Вопрос:\n" +
                "[Текст вопроса]\n\n" +
                "A) [Вариант 1]\n" +
                "B) [Вариант 2]\n" +
                "C) [Вариант 3]\n" +
                "D) [Вариант 4]\n\n" +
                "Ответ: [Правильная буква]\n" +
                "Слово: " + englishWord + " - " + translation + "\n\n" +

                "ТИП ВОПРОСА (вопрос и варианты должны быть связаны и хоть одна часть на английском):\n" +
                "- Вопрос на перевод: варианты - разные переводы\n" +
                "- Вопрос на определение: варианты - разные слова\n" +
                "- Вопрос на заполнение пропуска: варианты - разные слова для предложения\n" +
                "- Вопрос на синонимы: варианты - слова похожего значения\n\n" +

                "ПРИМЕР (вопрос и варианты - единое целое):\n" +
                "Вопрос:\n" +
                "Какое слово означает \"достижение\"?\n\n" +
                "A) failure\n" +
                "B) accomplishment\n" +
                "C) beginning\n" +
                "D) obstacle\n\n" +
                "Ответ: B\n" +
                "Слово: accomplishment - достижение";
    }
}