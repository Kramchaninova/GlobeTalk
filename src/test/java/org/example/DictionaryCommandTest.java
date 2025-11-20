package org.example;

import org.example.Dictionary.DictionaryCommand;
import org.example.Dictionary.DictionaryService;
import org.example.Dictionary.Word;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

/**
 * Проверяет функциональность работы со словарем: добавление, удаление,
 * редактирование слов и отображение словаря.
 * Использует in-memory реализацию для изоляции тестов.
 */
public class DictionaryCommandTest {

    /**
     * MockDictionaryService — in-memory реализация DictionaryService,
     * используемая в тестах для управления коллекцией слов без реальной БД.
     */
    public class MockDictionaryService implements DictionaryService {
        /**
         * Внутреннее представление слова в моковой базе.
         */
        private class DictionaryWord {
            int id;
            long userId;
            String englishWord;
            String translation;
            int priority;

            DictionaryWord(int id, long userId, String englishWord, String translation, int priority) {
                this.id = id;
                this.userId = userId;
                this.englishWord = englishWord;
                this.translation = translation;
                this.priority = priority;
            }
        }

        private final Map<Long, List<DictionaryWord>> storage = new HashMap<>();
        private int nextId = 1;

        public MockDictionaryService() {
            // Мок в оперативной памяти; файл базы данных не создается
        }

        @Override
        public void addWord(long userId, String englishWord, String translation, int priority) {
            storage.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(new DictionaryWord(nextId++, userId, englishWord, translation, priority));
        }

        @Override
        public List<Word> getAllWords(long userId) {
            return storage.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .map(w -> new Word(w.id, w.userId, w.englishWord, w.translation, w.priority))
                    .toList();
        }

        @Override
        public Word getWordById(long userId, int wordId) {
            return storage.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(w -> w.id == wordId && w.userId == userId)
                    .findFirst()
                    .map(w -> new Word(w.id, w.userId, w.englishWord, w.translation, w.priority))
                    .orElse(null);
        }

        @Override
        public Word getWordByEnglish(long userId, String englishWord) {
            String searchWord = englishWord.toLowerCase();
            return storage.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(w -> w.englishWord.toLowerCase().equals(searchWord) && w.userId == userId)
                    .findFirst()
                    .map(w -> new Word(w.id, w.userId, w.englishWord, w.translation, w.priority))
                    .orElse(null);
        }

        @Override
        public void updateWord(long userId, int wordId, String newEnglishWord, String newTranslation, Integer newPriority) {
            List<DictionaryWord> list = storage.getOrDefault(userId, Collections.emptyList());
            for (DictionaryWord w : list) {
                if (w.id == wordId && w.userId == userId) {
                    w.englishWord = newEnglishWord;
                    w.translation = newTranslation;
                    w.priority = newPriority != null ? newPriority : DEFAULT_PRIORITY;
                    return;
                }
            }
        }

        @Override
        public void deleteWord(long userId, int wordId) {
            List<DictionaryWord> list = storage.getOrDefault(userId, Collections.emptyList());
            list.removeIf(w -> w.id == wordId && w.userId == userId);
        }

        @Override
        public long getUserIdByChatId(long chatId) throws SQLException {
            return chatId; // В тестах используем chatId как userId
        }

        /**
         * Вспомогательный метод для получения внутреннего ID слова по индексу.
         * @param userId идентификатор пользователя
         * @param index индекс слова в списке
         * @return ID слова или null если индекс неверный
         */
        public Integer getWordIdByIndex(long userId, int index) {
            List<DictionaryWord> list = storage.getOrDefault(userId, Collections.emptyList());
            if (index < 1 || index > list.size()) return null;
            return list.get(index - 1).id;
        }

        /**
         * Вспомогательный метод для получения количества слов пользователя.
         * @param userId идентификатор пользователя
         * @return количество слов пользователя
         */
        public int getWordCount(long userId) {
            return storage.getOrDefault(userId, Collections.emptyList()).size();
        }
    }

    private DictionaryCommand dictionaryCommand;
    private MockDictionaryService mock;

    @BeforeEach
    public void setUp() {
        mock = new MockDictionaryService();
        dictionaryCommand = new DictionaryCommand(mock);
    }

    /**
     * Тест: отображение пустого словаря.
     */
    @Test
    public void showEmptyDictionary() {
        long userId = 100L;
        String result = dictionaryCommand.showDictionary(userId);

        Assertions.assertEquals("✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь пуст*\n" +
                "Добавьте первое слово для начала изучения!\n\n"+
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить перевод слова\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:", result);
    }

    /**
     * Тест: отображение словаря с словами.
     */
    @Test
    public void showDictionaryWithWords() {
        long userId = 101L;
        mock.addWord(userId, "apple", "яблоко", 2);
        mock.addWord(userId, "book", "книга", 2);

        String result = dictionaryCommand.showDictionary(userId);

        Assertions.assertEquals("✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь* (2 слов)\n\n" +
                "• apple - яблоко\n" +
                "• book - книга\n\n" +
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить слово или перевод\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:", result);
    }

    /**
     * Тест: добавление слова через текстовую команду.
     */
    @Test
    public void addWordViaTextCommand() {
        long userId = 102L;

        // Устанавливаем состояние ожидания добавления слова
        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);

        // Добавляем слово через пробел
        String result = dictionaryCommand.handleTextCommand("hello привет", userId);

        Assertions.assertEquals("🔤 *Новое слово добавлено!*\n\n" +
                "Слово: **hello**\n" +
                "Перевод: **привет**\n\n"+
                "✨ *Пополнить еще словарь?*", result);

        // Проверяем, что слово действительно добавлено в бд
        Assertions.assertEquals(1, mock.getWordCount(userId));
        Word addedWord = mock.getAllWords(userId).get(0);
        Assertions.assertEquals("hello", addedWord.getEnglishWord());
        Assertions.assertEquals("привет", addedWord.getTranslation());
    }

    /**
     * Тест: добавление фразы через тире.
     */
    @Test
    public void addPhraseWithDash() {
        long userId = 103L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);

        // Добавляем фразу через тире
        String result = dictionaryCommand.handleTextCommand("looking for - искать (находиться в поиске)", userId);

        Assertions.assertEquals("🔤 *Новое слово добавлено!*\n\n" +
                "Слово: **looking for**\n" +
                "Перевод: **искать (находиться в поиске)**\n\n"+
                "✨ *Пополнить еще словарь?*", result);

        Word addedWord = mock.getAllWords(userId).get(0);
        Assertions.assertEquals("looking for", addedWord.getEnglishWord());
        Assertions.assertEquals("искать (находиться в поиске)", addedWord.getTranslation());
    }

    /**
     * Тест: удаление слова с подтверждением.
     */
    @Test
    public void deleteWordWithConfirmation() {
        long userId = 104L;
        mock.addWord(userId, "test", "тест", 2);

        // Устанавливаем состояние ожидания удаления
        dictionaryCommand.handleButtonClick("dictionary_delete_button", userId);

        // Вводим слово для удаления
        String confirmationMessage = dictionaryCommand.handleTextCommand("test", userId);

        Assertions.assertEquals("🗑️ *Подтвердите удаление*\n\n" +
                "📝 Слово: **\"test\"**\n" +
                "🎯 Перевод: **\"тест\"**\n\n" +
                "✨ *Это слово было частью вашего языкового пути!*\n" +
                "❓ *Вы уверены, что хотите попрощаться с \"test\"?*\n\n" +
                "⚠️ *Напоминание:* после удаления слово исчезнет из всех ваших тренировок и больше не будет повторяться.\n\n" +
                "💫 *Принимайте взвешенное решение!*", confirmationMessage);

        // Подтверждаем удаление через кнопку
        String deleteResult = dictionaryCommand.handleButtonClick("dictionary_delete_confirm_button", userId);

        Assertions.assertEquals("✅ *Готово! Слово \"test\" удалено*\n\n" +
                "Теперь **\"тест\"** больше не будет появляться в вашем словареи в ваших тренировках.\n\n", deleteResult);
        Assertions.assertEquals(0, mock.getWordCount(userId));
    }

    /**
     * Тест: редактирование перевода слова.
     */
    @Test
    public void editWordTranslation() {
        long userId = 105L;
        mock.addWord(userId, "old", "старый", 2);
        int wordId = mock.getWordIdByIndex(userId, 1);

        // Устанавливаем состояние ожидания редактирования
        dictionaryCommand.handleButtonClick("dictionary_edit_button", userId);

        // Вводим слово для редактирования
        String editMessage = dictionaryCommand.handleTextCommand("old", userId);

        Assertions.assertEquals("✏️ *Редактирование перевода*\n\n" +
                "📝 Слово: **old**\n" +
                "🎯 Перевод: **старый**\n\n" +
                "💫 *Введите новый перевод:* 📝", editMessage);

        // Вводим новый перевод
        String updateResult = dictionaryCommand.handleTextCommand("новый", userId);

        Assertions.assertEquals("Отлично! Перевод успешно обновлён ✅\n\n" +
                "старый → новый\n" +
                "Слово сохранено в вашем словаре ✨", updateResult);

        // Проверяем, что перевод обновлен
        Word updatedWord = mock.getWordById(userId, wordId);
        Assertions.assertEquals("новый", updatedWord.getTranslation());
        Assertions.assertEquals("old", updatedWord.getEnglishWord()); // Английское слово не изменилось
    }

    /**
     * Тест: попытка удаления несуществующего слова.
     */
    @Test
    public void deleteNonExistentWord() {
        long userId = 106L;

        dictionaryCommand.handleButtonClick("dictionary_delete_button", userId);

        String result = dictionaryCommand.handleTextCommand("nonexistent", userId);

        Assertions.assertEquals("❌ *Неверный ввод слова!*\n\n" +
                "Возможно, вы ошиблись в написании или использовали неверный формат.\n\n" +
                "🔍 *Проверьте:*\n" +
                "• Нет ли опечаток в слове?\n" +
                "• Не добавили ли вы перевод?\n" +
                "• Правильно ли указали язык слова?\n\n" +
                "💫 *Попробуйте еще раз - я всегда готов помочь!*", result);
    }

    /**
     * Тест: отмена удаления слова.
     */
    @Test
    public void cancelWordDeletion() {
        long userId = 107L;
        mock.addWord(userId, "cancel", "отмена", 2);

        String result = dictionaryCommand.handleButtonClick("dictionary_delete_cancel_button", userId);

        Assertions.assertEquals("💫 *Удаление отменено*\n\n" +
                "Слово осталось в вашем словаре и продолжит появляться в тренировках.\n\n" +
                "✨ *Что дальше?*\n" +
                "• 🗑️ Продолжить удаление других слов\n" +
                "• 📚 Вернуться к изучению\n" +
                "• 👀 Посмотреть словарь\n\n" +
                "🌱 *Иногда сохранить - тоже важное решение!*", result);

        // Проверяем, что слово не удалено
        Assertions.assertEquals(1, mock.getWordCount(userId));
    }

    /**
     * Тест: обработка неизвестной команды.
     */
    @Test
    public void handleUnknownCommand() {
        long userId = 108L;

        String result = dictionaryCommand.handleButtonClick("unknown_command", userId);

        Assertions.assertEquals("Неизвестная команда", result);
    }

    /**
     * Тест: добавление слова с приоритетом по умолчанию.
     */
    @Test
    public void addWordWithDefaultPriority() {
        long userId = 109L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        dictionaryCommand.handleTextCommand("word перевод", userId);

        Word addedWord = mock.getAllWords(userId).get(0);
        Assertions.assertEquals(2, addedWord.getPriority()); // константа DEFAULT_PRIORITY = 2
    }

    /**
     * Тест: навигация по кнопкам словаря.
     */
    @Test
    public void dictionaryNavigation() {
        long userId = 110L;

        // Переход к добавлению слова
        String addResult = dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        Assertions.assertEquals("📝 *Как добавить слово:*\n\n" +
                "Просто отправьте мне слово на иностранном языке, а затем его перевод через пробел.\n" +
                "А если хотите добавить фразу и перевод, то введите их через тире ('-') \n\n" +
                "*Например:*\n" +
                "`apple - яблоко`\n" +
                "`looking for - искать (находиться в поиске)`", addResult);

        // Переход к редактированию
        String editResult = dictionaryCommand.handleButtonClick("dictionary_edit_button", userId);
        Assertions.assertEquals("🔤 Редактирование перевода\n" +
                "Чтобы отредактировать слово, введите его на английском языке " +
                "в точности так, как оно указано в словаре. Изменить можно только " +
                "его перевод на русский язык.", editResult);

        // Переход к удалению
        String deleteResult = dictionaryCommand.handleButtonClick("dictionary_delete_button", userId);
        Assertions.assertEquals("🗑️ *Как удалить слово:*\n\n" +
                "Просто отправьте мне слово на английском (без перевода), которое хотите удалить из словаря.\n\n" +
                "*Например:*\n" +
                "вы хотите удалить \"apple - яблоко\"\n" +
                "введите: \"apple\"\n\n" +
                "✨ *После удаления слово перестанет появляться в ваших тренировках!*", deleteResult);

        // Возврат к словарю после добавления
        String backResult = dictionaryCommand.handleButtonClick("dictionary_add_no_button", userId);
        Assertions.assertEquals("✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь пуст*\n" +
                "Добавьте первое слово для начала изучения!\n\n"+
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить перевод слова\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:", backResult);
    }

    /**
     * Тест: обработка некорректного формата при добавлении.
     */
    @Test
    public void handleInvalidAddFormat() {
        long userId = 111L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);

        // Некорректный формат - только одно слово
        String result = dictionaryCommand.handleTextCommand("singleword", userId);

        Assertions.assertEquals("❌ Неправильный ввод или команда", result);

        // Проверяем, что слово не было добавлено в базу данных
        Assertions.assertEquals(0, mock.getWordCount(userId));
    }
}