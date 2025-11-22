package org.example.ScheduledMessages;

import java.util.*;

/**
 * MessageParser - класс для парсинга структурированных учебных материалов
 * Использует построчный парсинг вместо регулярных выражений
 */
public class MessageParser {

    /**
     * Парсит текст с одним словом и возвращает WordData
     * Возвращает null если не удалось распарсить
     */
    public WordData parseWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String[] lines = text.split("\n");
        Map<String, String> fields = new HashMap<>();

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("WORD:") || line.startsWith("TRANSLATION:") ||
                    line.startsWith("LEVEL:") || line.startsWith("PART_OF_SPEECH:") ||
                    line.startsWith("EXAMPLE:") || line.startsWith("EXAMPLE_TRANSLATION:") ||
                    line.startsWith("RELATED_WORDS:") || line.startsWith("TOPIC:")) {

                // Извлекаем ключ и значение
                int colonIndex = line.indexOf(":");
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    fields.put(key, value);
                }
            }
        }

        return createWordData(fields);
    }

    /**
     * Создает объект WordData из Map
     */
    private WordData createWordData(Map<String, String> fields) {
        if (!fields.containsKey("WORD") || !fields.containsKey("TRANSLATION")) {
            return null; // Обязательные поля отсутствуют
        }

        WordData wordData = new WordData();

        wordData.setWord(fields.get("WORD"));
        wordData.setTranslation(fields.get("TRANSLATION"));
        wordData.setLevel(fields.get("LEVEL"));
        wordData.setPartOfSpeech(fields.get("PART_OF_SPEECH"));
        wordData.setExample(fields.get("EXAMPLE"));
        wordData.setExampleTranslation(fields.get("EXAMPLE_TRANSLATION"));
        wordData.setTopic(fields.get("TOPIC"));

        // Парсим связанные слова
        String relatedWords = fields.get("RELATED_WORDS");
        if (relatedWords != null) {
            wordData.setRelatedWords(parseCommaSeparated(relatedWords));
        } else {
            wordData.setRelatedWords(new ArrayList<>());
        }

        return wordData;
    }

    /**
     * Парсит строку с значениями через запятую
     */
    private List<String> parseCommaSeparated(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(str.split("\\s*,\\s*"));
    }
}