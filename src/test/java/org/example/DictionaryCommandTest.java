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
        public void addWord(long userId, String englishWord, String translation, int priority) throws SQLException {
            // Проверяем, нет ли уже такого слова
            if (getWordByEnglish(userId, englishWord) != null) {
                throw new SQLException("Слово уже существует в словаре");
            }

            storage.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(new DictionaryWord(nextId++, userId, englishWord, translation, priority));
        }

        @Override
        public List<Word> getAllWords(long userId) throws SQLException {
            return storage.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .map(w -> new Word(w.id, w.userId, w.englishWord, w.translation, w.priority))
                    .toList();
        }

        @Override
        public List<Word> getWordsByPriority(long userId, int priority) throws SQLException {
            return storage.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(w -> w.priority == priority)
                    .map(w -> new Word(w.id, w.userId, w.englishWord, w.translation, w.priority))
                    .toList();
        }

        @Override
        public Word getWordById(long userId, int wordId) throws SQLException {
            return storage.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(w -> w.id == wordId && w.userId == userId)
                    .findFirst()
                    .map(w -> new Word(w.id, w.userId, w.englishWord, w.translation, w.priority))
                    .orElse(null);
        }

        @Override
        public Word getWordByEnglish(long userId, String englishWord) throws SQLException {
            String searchWord = englishWord.toLowerCase();
            return storage.getOrDefault(userId, Collections.emptyList())
                    .stream()
                    .filter(w -> w.englishWord.toLowerCase().equals(searchWord) && w.userId == userId)
                    .findFirst()
                    .map(w -> new Word(w.id, w.userId, w.englishWord, w.translation, w.priority))
                    .orElse(null);
        }

        @Override
        public void updateWord(long userId, int wordId, String newEnglishWord, String newTranslation, Integer newPriority) throws SQLException {
            List<DictionaryWord> list = storage.getOrDefault(userId, Collections.emptyList());
            for (DictionaryWord w : list) {
                if (w.id == wordId && w.userId == userId) {
                    w.englishWord = newEnglishWord;
                    w.translation = newTranslation;
                    w.priority = newPriority != null ? newPriority : 2;
                    return;
                }
            }
            throw new SQLException("Слово не найдено для обновления");
        }

        @Override
        public void updateWordPriority(long userId, int wordId, int newPriority) throws SQLException {
            List<DictionaryWord> list = storage.getOrDefault(userId, Collections.emptyList());
            for (DictionaryWord w : list) {
                if (w.id == wordId && w.userId == userId) {
                    w.priority = newPriority;
                    return;
                }
            }
            throw new SQLException("Слово не найдено для обновления приоритета");
        }

        @Override
        public void deleteWord(long userId, int wordId) throws SQLException {
            List<DictionaryWord> list = storage.getOrDefault(userId, Collections.emptyList());
            boolean removed = list.removeIf(w -> w.id == wordId && w.userId == userId);
            if (!removed) {
                throw new SQLException("Слово не найдено для удаления");
            }
        }

        @Override
        public long getUserIdByChatId(long chatId) throws SQLException {
            return chatId;
        }

        public Integer getWordIdByIndex(long userId, int index) {
            List<DictionaryWord> list = storage.getOrDefault(userId, Collections.emptyList());
            if (index < 1 || index > list.size()) return null;
            return list.get(index - 1).id;
        }

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

        String expected = "✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь пуст*\n" +
                "Добавьте первое слово для начала изучения!\n\n"+
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить перевод слова\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:";

        Assertions.assertEquals(expected, result);
    }

    /**
     * Тест: отображение словаря с словами.
     */
    @Test
    public void showDictionaryWithWords() throws SQLException {
        long userId = 101L;
        mock.addWord(userId, "apple", "яблоко", 2);
        mock.addWord(userId, "book", "книга", 2);

        String result = dictionaryCommand.showDictionary(userId);

        String expected = "✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь* (2 слов)\n\n" +
                "• apple - яблоко\n" +
                "• book - книга\n\n" +
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить слово или перевод\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:";

        Assertions.assertEquals(expected, result);
    }

    /**
     * Тест: добавление слова через текстовую команду.
     */
    @Test
    public void addWordViaTextCommand() throws SQLException {
        long userId = 102L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String result = dictionaryCommand.handleTextCommand("hello привет", userId);

        String expected = "🔤 *Новое слово добавлено!*\n\n" +
                "Слово: **hello**\n" +
                "Перевод: **привет**\n\n"+
                "✨ *Пополнить еще словарь?*";

        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(1, mock.getWordCount(userId));

        Word addedWord = mock.getAllWords(userId).get(0);
        Assertions.assertEquals("hello", addedWord.getEnglishWord());
        Assertions.assertEquals("привет", addedWord.getTranslation());
        Assertions.assertEquals(2, addedWord.getPriority());
    }

    /**
     * Тест: добавление фразы через тире.
     */
    @Test
    public void addPhraseWithDash() throws SQLException {
        long userId = 103L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String result = dictionaryCommand.handleTextCommand("looking for - искать (находиться в поиске)", userId);

        String expected = "🔤 *Новое слово добавлено!*\n\n" +
                "Слово: **looking for**\n" +
                "Перевод: **искать (находиться в поиске)**\n\n"+
                "✨ *Пополнить еще словарь?*";

        Assertions.assertEquals(expected, result);

        Word addedWord = mock.getAllWords(userId).get(0);
        Assertions.assertEquals("looking for", addedWord.getEnglishWord());
        Assertions.assertEquals("искать (находиться в поиске)", addedWord.getTranslation());
    }

    /**
     * Тест: добавление слова с дубликатом.
     */
    @Test
    public void addDuplicateWord() throws SQLException {
        long userId = 104L;
        mock.addWord(userId, "duplicate", "дубликат", 2);

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String result = dictionaryCommand.handleTextCommand("duplicate дубликат", userId);

        Assertions.assertEquals("❌ Ошибка при добавлении слова: Слово уже существует в словаре", result);
        Assertions.assertEquals(1, mock.getWordCount(userId));
    }

    /**
     * Тест: удаление слова с подтверждением.
     */
    @Test
    public void deleteWordWithConfirmation() throws SQLException {
        long userId = 105L;
        mock.addWord(userId, "test", "тест", 2);

        dictionaryCommand.handleButtonClick("dictionary_delete_button", userId);
        String confirmationMessage = dictionaryCommand.handleTextCommand("test", userId);

        String expectedConfirmation = "🗑️ *Подтвердите удаление*\n\n" +
                "📝 Слово: **\"test\"**\n" +
                "🎯 Перевод: **\"тест\"**\n\n" +
                "✨ *Это слово было частью вашего языкового пути!*\n" +
                "❓ *Вы уверены, что хотите попрощаться с \"test\"?*\n\n" +
                "⚠️ *Напоминание:* после удаления слово исчезнет из всех ваших тренировок и больше не будет повторяться.\n\n" +
                "💫 *Принимайте взвешенное решение!*";

        Assertions.assertEquals(expectedConfirmation, confirmationMessage);

        String deleteResult = dictionaryCommand.handleButtonClick("dictionary_delete_confirm_button", userId);
        String expectedDelete = "✅ *Готово! Слово \"test\" удалено*\n\n" +
                "Теперь **\"тест\"** больше не будет появляться в вашем словаре" +
                "и в ваших тренировках.\n\n";

        Assertions.assertEquals(expectedDelete, deleteResult);
        Assertions.assertEquals(0, mock.getWordCount(userId));
    }

    /**
     * Тест: редактирование перевода слова.
     */
    @Test
    public void editWordTranslation() throws SQLException {
        long userId = 106L;
        mock.addWord(userId, "old", "старый", 2);
        int wordId = mock.getWordIdByIndex(userId, 1);

        dictionaryCommand.handleButtonClick("dictionary_edit_button", userId);
        String editMessage = dictionaryCommand.handleTextCommand("old", userId);

        String expectedEditMessage = "✏️ *Редактирование перевода*\n\n" +
                "📝 Слово: **old**\n" +
                "🎯 Перевод: **старый**\n\n" +
                "💫 *Введите новый перевод:* 📝";

        Assertions.assertEquals(expectedEditMessage, editMessage);

        String updateResult = dictionaryCommand.handleTextCommand("новый", userId);
        String expectedUpdate = "Отлично! Перевод успешно обновлён ✅\n\n" +
                "старый → новый\n" +
                "Слово сохранено в вашем словаре ✨";

        Assertions.assertEquals(expectedUpdate, updateResult);

        Word updatedWord = mock.getWordById(userId, wordId);
        Assertions.assertEquals("новый", updatedWord.getTranslation());
        Assertions.assertEquals("old", updatedWord.getEnglishWord());
    }

    /**
     * Тест: попытка удаления несуществующего слова.
     */
    @Test
    public void deleteNonExistentWord() {
        long userId = 107L;

        dictionaryCommand.handleButtonClick("dictionary_delete_button", userId);
        String result = dictionaryCommand.handleTextCommand("nonexistent", userId);

        String expected = "❌ *Неверный ввод слова!*\n\n" +
                "Возможно, вы ошиблись в написании или использовали неверный формат.\n\n" +
                "🔍 *Проверьте:*\n" +
                "• Нет ли опечаток в слове?\n" +
                "• Не добавили ли вы перевод?\n" +
                "• Правильно ли указали язык слова?\n\n" +
                "💫 *Попробуйте еще раз - я всегда готов помочь!*";

        Assertions.assertEquals(expected, result);
    }

    /**
     * Тест: отмена удаления слова.
     */
    @Test
    public void cancelWordDeletion() throws SQLException {
        long userId = 108L;
        mock.addWord(userId, "cancel", "отмена", 2);

        String result = dictionaryCommand.handleButtonClick("dictionary_delete_cancel_button", userId);

        String expected = "💫 *Удаление отменено*\n\n" +
                "Слово осталось в вашем словаре и продолжит появляться в тренировках.\n\n" +
                "✨ *Что дальше?*\n" +
                "• 🗑️ Продолжить удаление других слов\n" +
                "• 📚 Вернуться к изучению\n" +
                "• 👀 Посмотреть словарь\n\n" +
                "🌱 *Иногда сохранить - тоже важное решение!*";

        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(1, mock.getWordCount(userId));
    }

    /**
     * Тест: обработка неизвестной команды.
     */
    @Test
    public void handleUnknownCommand() {
        long userId = 109L;

        String result = dictionaryCommand.handleButtonClick("unknown_command", userId);

        Assertions.assertEquals("Неизвестная команда", result);
    }

    /**
     * Тест: навигация по кнопкам словаря.
     */
    @Test
    public void dictionaryNavigation() {
        long userId = 110L;

        String addResult = dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String expectedAdd = "📝 *Как добавить слово:*\n\n" +
                "Просто отправьте мне слово на иностранном языке, а затем его перевод через пробел.\n" +
                "А если хотите добавить фразу и перевод, то введите их через тире ('-') \n\n" +
                "*Например:*\n" +
                "`apple - яблоко`\n" +
                "`looking for - искать (находиться в поиске)`";
        Assertions.assertEquals(expectedAdd, addResult);

        String editResult = dictionaryCommand.handleButtonClick("dictionary_edit_button", userId);
        String expectedEdit = "🔤 Редактирование перевода\n" +
                "Чтобы отредактировать слово, введите его на английском языке " +
                "в точности так, как оно указано в словаре. Изменить можно только " +
                "его перевод на русский язык.";
        Assertions.assertEquals(expectedEdit, editResult);

        String deleteResult = dictionaryCommand.handleButtonClick("dictionary_delete_button", userId);
        String expectedDelete = "🗑️ *Как удалить слово:*\n\n" +
                "Просто отправьте мне слово на английском (без перевода), которое хотите удалить из словаря.\n\n" +
                "*Например:*\n" +
                "вы хотите удалить \"apple - яблоко\"\n" +
                "введите: \"apple\"\n\n" +
                "✨ *После удаления слово перестанет появляться в ваших тренировках!*";
        Assertions.assertEquals(expectedDelete, deleteResult);

        String backResult = dictionaryCommand.handleButtonClick("dictionary_add_no_button", userId);
        String expectedBack = "✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                "📚 *Ваш словарь пуст*\n" +
                "Добавьте первое слово для начала изучения!\n\n"+
                "🛠️ *Доступные действия:*\n\n" +
                "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                "• ✏️ **Редактировать** — изменить перевод слова\n" +
                "• ❌ **Удалить слово** — убрать из словаря\n" +
                "• ↩️ **Назад** — вернуться в меню\n\n" +
                "Выберите действие:";
        Assertions.assertEquals(expectedBack, backResult);
    }

    /**
     * Тест: обработка некорректного формата при добавлении.
     */
    @Test
    public void handleInvalidAddFormat() {
        long userId = 111L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String result = dictionaryCommand.handleTextCommand("singleword", userId);

        Assertions.assertEquals("❌ Неправильный ввод или команда", result);
        Assertions.assertEquals(0, mock.getWordCount(userId));
    }

    /**
     * Тест: получение слов по приоритету.
     */
    @Test
    public void testGetWordsByPriority() throws SQLException {
        long userId = 112L;
        mock.addWord(userId, "word1", "перевод1", 1);
        mock.addWord(userId, "word2", "перевод2", 2);
        mock.addWord(userId, "word3", "перевод3", 1);

        List<Word> priority1Words = mock.getWordsByPriority(userId, 1);
        Assertions.assertEquals(2, priority1Words.size());
        Assertions.assertEquals("word1", priority1Words.get(0).getEnglishWord());
        Assertions.assertEquals("word3", priority1Words.get(1).getEnglishWord());

        List<Word> priority2Words = mock.getWordsByPriority(userId, 2);
        Assertions.assertEquals(1, priority2Words.size());
        Assertions.assertEquals("word2", priority2Words.get(0).getEnglishWord());
    }

    /**
     * Тест: обновление приоритета слова.
     */
    @Test
    public void testUpdateWordPriority() throws SQLException {
        long userId = 113L;
        mock.addWord(userId, "test", "тест", 1);
        int wordId = mock.getWordIdByIndex(userId, 1);

        mock.updateWordPriority(userId, wordId, 5);

        Word updatedWord = mock.getWordById(userId, wordId);
        Assertions.assertEquals(5, updatedWord.getPriority());
    }

    /**
     * Тест: обновление слова с изменением английского слова.
     */
    @Test
    public void testUpdateWordWithEnglishChange() throws SQLException {
        long userId = 114L;
        mock.addWord(userId, "oldword", "старое", 2);
        int wordId = mock.getWordIdByIndex(userId, 1);

        mock.updateWord(userId, wordId, "newword", "новое", 3);

        Word updatedWord = mock.getWordById(userId, wordId);
        Assertions.assertEquals("newword", updatedWord.getEnglishWord());
        Assertions.assertEquals("новое", updatedWord.getTranslation());
        Assertions.assertEquals(3, updatedWord.getPriority());
    }

    /**
     * Тест: обработка SQLException при операциях.
     */
    @Test
    public void testSQLExceptionHandling() throws SQLException {
        long userId = 115L;

        Assertions.assertThrows(SQLException.class, () -> {
            mock.deleteWord(userId, 999);
        });

        Assertions.assertThrows(SQLException.class, () -> {
            mock.updateWord(userId, 999, "new", "новый", 2);
        });
    }

    /**
     * Тест: сброс состояния пользователя.
     */
    @Test
    public void testResetUserState() throws SQLException {
        long userId = 116L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        dictionaryCommand.resetUserState(userId);

        String result = dictionaryCommand.handleTextCommand("test", userId);
        Assertions.assertEquals("❌ Неправильный ввод или команда", result);
    }

    /**
     * Тест: обработка пустого ввода при удалении.
     */
    @Test
    public void testEmptyInputForDelete() {
        long userId = 117L;

        dictionaryCommand.handleButtonClick("dictionary_delete_button", userId);
        String result = dictionaryCommand.handleTextCommand("", userId);

        Assertions.assertEquals("❌ Пожалуйста, введите корректное слово", result);
    }

    /**
     * Тест: обработка поиска несуществующего слова при редактировании.
     */
    @Test
    public void testEditNonExistentWord() {
        long userId = 118L;

        dictionaryCommand.handleButtonClick("dictionary_edit_button", userId);
        String result = dictionaryCommand.handleTextCommand("nonexistent", userId);

        Assertions.assertEquals("❌ Вы ничего не ввели", result);
    }


    /**
     * Тест: обработка некорректного формата при добавлении через тире.
     */
    @Test
    public void handleInvalidDashFormat() {
        long userId = 119L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String result = dictionaryCommand.handleTextCommand(" - ", userId);

        Assertions.assertEquals("❌ Неправильный ввод или команда", result);
        Assertions.assertEquals(0, mock.getWordCount(userId));
    }

    /**
     * Тест: обработка слишком длинного ввода при добавлении.
     */
    @Test
    public void handleTooManyWordsInAdd() {
        long userId = 120L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String result = dictionaryCommand.handleTextCommand("word1 word2 word3 word4", userId);
        //дело в том что он сплитует по первому пробелу между словами
        Assertions.assertEquals("🔤 *Новое слово добавлено!*\n\n" +
                "Слово: **word1**\n" +
                "Перевод: **word2 word3 word4**\n\n" +
                "✨ *Пополнить еще словарь?*", result);
        Assertions.assertEquals(1, mock.getWordCount(userId));
    }

    /**
     * Тест: добавление слова с пробелами в начале и конце.
     */
    @Test
    public void addWordWithTrimSpaces() throws SQLException {
        long userId = 121L;

        dictionaryCommand.handleButtonClick("dictionary_add_button", userId);
        String result = dictionaryCommand.handleTextCommand("  hello   привет  ", userId);

        String expected = "🔤 *Новое слово добавлено!*\n\n" +
                "Слово: **hello**\n" +
                "Перевод: **привет**\n\n"+
                "✨ *Пополнить еще словарь?*";

        Assertions.assertEquals(expected, result);

        Word addedWord = mock.getAllWords(userId).get(0);
        Assertions.assertEquals("hello", addedWord.getEnglishWord());
        Assertions.assertEquals("привет", addedWord.getTranslation());
    }
}