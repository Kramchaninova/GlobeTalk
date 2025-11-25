package org.example.ScheduledNewWord;

import org.example.OpenRouter.OpenRouterClient;
import org.example.Tokens.TokenOpenRouter;

import java.io.IOException;

/**
 * Хранит запрос (PROMT),
 * подгружает api token openrouter,
 * вызывает метод отправки нашего запроса непосредственно ии
 */
public class ScheduleGenerateMessage {
    //наш запрос
    private static final String PROMPT= "Генерируй учебные материалы для изучения английского языка СТРОГО в указанном формате без каких-либо дополнительных комментариев или пояснений.\n" +
            "\n" +
            "**ФОРМАТ ДЛЯ СЛОВА:**\n" +
            "WORD: [английское слово]\n" +
            "TRANSLATION: [перевод на русский и только ОДНО слово]\n" +
            "LEVEL: [A1/A2/B1/B2/C1/C2]\n" +
            "PART_OF_SPEECH: [noun/verb/adjective/adverb/preposition/conjunction]\n" +
            "EXAMPLE: [пример предложения на английском]\n" +
            "EXAMPLE_TRANSLATION: [перевод примера на русский]\n" +
            "RELATED_WORDS: [слово1, слово2, слово3]\n" +
            "TOPIC: [тема/категория]\n" +
            "\n" +
            "**ТРЕБОВАНИЯ:**\n" +
            "- Уровень сложности должен соответствовать реальной сложности слова\n" +
            "- Примеры должны быть практичными и полезными для повседневного общения\n" +
            "- Темы должны охватывать разные сферы жизни\n" +
            "- Переводы должны быть точными и естественными\n" +
            "- RELATED_WORDS должны быть действительно связаны по смыслу или словообразованию\n" +
            "\n" +
            "**СФЕРЫ ДЛЯ ТЕМ:**\n" +
            "- Повседневная жизнь (everyday life)\n" +
            "- Работа и карьера (work & career)\n" +
            "- Путешествия (travel)\n" +
            "- Еда и кулинария (food & cooking)\n" +
            "- Технологии (technology)\n" +
            "- Здоровье (health)\n" +
            "- Образование (education)\n" +
            "- Хобби и развлечения (hobbies & entertainment)\n" +
            "\n" +
            "Сгенерируй ТОЛЬКО 1 слово";

    /**
     * Метод для генерации теста и сохранение в txt файл
     */
    public String generateWord() {
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
