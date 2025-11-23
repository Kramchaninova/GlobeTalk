package org.example.ScheduledNewWord;

import org.example.Dictionary.DictionaryService;
import org.example.Dictionary.DictionaryServiceImpl;
import org.example.Dictionary.Word;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message - класс для работы с генерацией слов и словарем
 */
public class Message {
    private final MessageParser messageParser;
    private final ScheduleGenerateMessage generator;
    private final DictionaryService dictionaryService;

    // Храним последние показанные слова для каждого пользователя
    private final ConcurrentHashMap<Long, WordData> lastShownWords = new ConcurrentHashMap<>();

    public Message() {
        this.messageParser = new MessageParser();
        this.generator = new ScheduleGenerateMessage();
        this.dictionaryService = new DictionaryServiceImpl();
        System.out.println("✅ Message service инициализирован");
    }

    /**
     * Генерирует уникальное слово для пользователя (проверяет словарь)
     */
    public String getUniqueWordForUser(long chatId) {
        try {
            long userId = dictionaryService.getUserIdByChatId(chatId);

            // Пытаемся сгенерировать уникальное слово (максимум 10 попыток)
            for (int attempt = 0; attempt < 10; attempt++) {
                WordData wordData = generateWord();
                if (wordData == null) {
                    return "❌ Не удалось сгенерировать слово";
                }

                // Проверяем, есть ли такое слово в словаре пользователя
                Word existingWord = dictionaryService.getWordByEnglish(userId, wordData.getWord());

                if (existingWord == null) {
                    // Слово уникальное - используем его
                    System.out.println("Сгенерировано уникальное слово для пользователя " + chatId + " (userId: " + userId + "): " + wordData.getWord());

                    // СРАЗУ добавляем слово в словарь с приоритетом 5 (изучаю)
                    try {
                        dictionaryService.addWord(userId, wordData.getWord(), wordData.getTranslation(), 5);
                        System.out.println("Слово '" + wordData.getWord() + "' добавлено в словарь с приоритетом 5");
                    } catch (SQLException e) {
                        System.err.println("Ошибка добавления слова в словарь: " + e.getMessage());
                    }

                    // Сохраняем последнее показанное слово
                    lastShownWords.put(chatId, wordData);

                    return formatWordMessage(wordData);
                } else {
                    // Слово уже есть в словаре - проверяем перевод
                    if (!existingWord.getTranslation().equals(wordData.getTranslation())) {
                        // Переводы разные - оставляем слово с сообщением
                        String baseMessage = formatWordMessage(wordData);
                        String messageWithNote = baseMessage + "\n\n💡 **Примечание:** Это слово уже есть в вашем словаре, но с другим переводом!";
                        System.out.println("Слово '" + wordData.getWord() + "' уже есть в словаре, но перевод отличается. Оставляем.");

                        // Сохраняем последнее показанное слово
                        lastShownWords.put(chatId, wordData);

                        return messageWithNote;
                    } else {
                        // Слово и перевод одинаковые - генерируем новое слово
                        System.out.println("Слово '" + wordData.getWord() + "' уже есть в словаре с таким же переводом. Генерируем новое. Попытка: " + (attempt + 1));
                    }
                }
            }

            return "❌ Не удалось сгенерировать уникальное слово после 10 попыток";

        } catch (SQLException e) {
            System.err.println("Ошибка проверки словаря для chatId " + chatId + ": " + e.getMessage());
            return "❌ Ошибка при проверке словаря. Попробуйте позже.";
        }
    }

    /**
     * Обновляет приоритет слова в словаре
     */
    private void updateWordPriority(long chatId, String englishWord, int newPriority) {
        try {
            long userId = dictionaryService.getUserIdByChatId(chatId);

            // Получаем текущее слово
            Word existingWord = dictionaryService.getWordByEnglish(userId, englishWord);
            if (existingWord == null) {
                System.err.println("Слово '" + englishWord + "' не найдено в словаре пользователя " + chatId);
                return;
            }

            // Обновляем приоритет
            dictionaryService.updateWord(userId, existingWord.getId(), englishWord, existingWord.getTranslation(), newPriority);

            String status = (newPriority == 2) ? "знаю" : "изучаю";
            System.out.println("Приоритет слова '" + englishWord + "' изменен на " + newPriority + " (" + status + ") для пользователя " + chatId);

        } catch (SQLException e) {
            System.err.println("Ошибка обновления приоритета слова для chatId " + chatId + ": " + e.getMessage());
        }
    }

    /**
     * Генерирует одно слово
     */
    private WordData generateWord() {
        try {
            String generatedText = generator.generateWord();

            if (generatedText == null || generatedText.isEmpty()) {
                System.err.println("Не удалось сгенерировать слово");
                return null;
            }

            WordData wordData = messageParser.parseWord(generatedText);

            if (wordData == null) {
                System.err.println("Не удалось распарсить сгенерированное слово");
                return null;
            }

            return wordData;

        } catch (Exception e) {
            System.err.println("Ошибка при генерации слова: " + e.getMessage());
            return null;
        }
    }

    /**
     * Форматирует WordData для отправки пользователя
     */
    public String formatWordMessage(WordData wordData) {
        if (wordData == null) {
            return "❌ Не удалось сгенерировать слово";
        }

        return "🎉 **Новое слово!** 🎉\n\n" +
                wordData.toString() +
                "\n\n✨ Учи с удовольствием!\n" +
                "Если вы знаете данное слово нажимай на кнопки \"Знаю\", иначе \"Изучаю\"";
    }

    /**
     * Обработка нажатия кнопок слов
     */
    public String handleWordButtonClick(String callbackData, long chatId) {
        System.out.println("Message: обработка кнопки '" + callbackData + "' для пользователя " + chatId);

        try {
            // Проверяем, что пользователь существует перед обработкой кнопки
            long userId = dictionaryService.getUserIdByChatId(chatId);
            System.out.println("Пользователь найден: chatId=" + chatId + ", userId=" + userId);
        } catch (SQLException e) {
            System.err.println("Пользователь не найден для chatId: " + chatId);
            return "❌ Ошибка: пользователь не найден. Пожалуйста, зарегистрируйтесь в системе.";
        }

        // Получаем последнее показанное слово
        WordData lastWord = lastShownWords.get(chatId);
        if (lastWord == null) {
            return "❌ Нет активного слова для обработки. Сначала получите слово через /word";
        }

        // Обрабатываем кнопки
        if (callbackData.equals("know_button")) {
            // Для "знаю" - меняем приоритет предыдущего слова с 5 на 2
            updateWordPriority(chatId, lastWord.getWord(), 2);

            // Показываем новое слово
            String newWord = getUniqueWordForUser(chatId);
            return "✅Здорово!\n" +
                    "Вот тогда другое слово для изучения...\n\n" + newWord;

        } else if (callbackData.equals("learn_button")) {
            // Для "изучаю" - слово уже добавлено с приоритетом 5
            return "✅ Слово уже добавлено в словарь для изучения!\n" +
                    "Посмотрите все слова в словаре или изучайте еще...";

        }else if (callbackData.equals("more_word_button")) {
            return getUniqueWordForUser(chatId);
        }
        else {
            return "❌ Неизвестная команда кнопки";
        }
    }
}