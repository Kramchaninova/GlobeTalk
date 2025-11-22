package org.example.ScheduledMessages;
import java.util.List;

/**
 * WordData - класс для хранения данных об отложенном слове
 */
public class WordData {
    private String word;
    private String translation;
    private String level;
    private String partOfSpeech;
    private String example;
    private String exampleTranslation;
    private List<String> relatedWords;
    private String topic;

    /**
     * @return английское слово
     */
    public String getWord() { return word; }
    /**
     * @param word английское слово
     */
    public void setWord(String word) { this.word = word; }

    /**
     * @return перевод на русский язык
     */
    public String getTranslation() { return translation; }
    /**
     * @param translation перевод на русский язык
     */
    public void setTranslation(String translation) { this.translation = translation; }

    /**
     * @return уровень сложности (A1, A2, B1, B2, C1, C2)
     */
    public String getLevel() { return level; }
    /**
     * @param level уровень сложности (A1, A2, B1, B2, C1, C2)
     */
    public void setLevel(String level) { this.level = level; }

    /**
     * @return часть речи (существительное, глагол, прилагательное и т.д.)
     */
    public String getPartOfSpeech() { return partOfSpeech; }
    /**
     * @param partOfSpeech часть речи (существительное, глагол, прилагательное и т.д.)
     */
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    /**
     * @return пример предложения с словом
     */
    public String getExample() { return example; }
    /**
     * @param example пример предложения с словом
     */
    public void setExample(String example) { this.example = example; }

    /**
     * @return перевод примера использования
     */
    public String getExampleTranslation() { return exampleTranslation; }
    /**
     * @param exampleTranslation перевод примера использования
     */
    public void setExampleTranslation(String exampleTranslation) { this.exampleTranslation = exampleTranslation; }

    /**
     * @return список похожих слов
     */
    public List<String> getRelatedWords() { return relatedWords; }
    /**
     * @param relatedWords список похожих слов
     */
    public void setRelatedWords(List<String> relatedWords) { this.relatedWords = relatedWords; }

    /**
     * @return тематика слова
     */
    public String getTopic() { return topic; }
    /**
     * @param topic тематика слова
     */
    public void setTopic(String topic) { this.topic = topic; }

    @Override
    public String toString() {
        String relatedWordsStr = "";
        if (relatedWords != null && !relatedWords.isEmpty()) {
            for (String word : relatedWords) {
                if (!relatedWordsStr.isEmpty()) {
                    relatedWordsStr += ", ";
                }
                relatedWordsStr += word;
            }
        }

        return String.format(
                "📚 СЛОВО: %s\n🎯 Перевод: %s\n📊 Уровень: %s\n🔤 Часть речи: %s\n💫 Пример: %s\n🌍 Перевод примера: %s\n✨ Похожие слова: %s\n🏷️ Тема: %s",
                word, translation, level, partOfSpeech, example, exampleTranslation,
                relatedWordsStr, topic
        );
    }
}