package org.example.Dictionary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DictionaryCommand - обрабатывает команды и кнопки словаря.
 * Управляет взаимодействием пользователя со словарем.
 */
public class DictionaryCommand {
    private final DictionaryService dictionaryService;
    private final Map<Long, String> userStates = new HashMap<>(); // chatId -> "delete:wordId" или "edit:wordId"

    //Константа приоритета по умолчанию
    private static final int DEFAULT_PRIORITY = 2;

    private static final String DICTIONARY_EMPTY =
            "✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n" +
                    "Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n" +
                    "📚 *Ваш словарь пуст*\n" +
                    "Добавьте первое слово для начала изучения!\n\n"+
                    "🛠️ *Доступные действия:*\n\n" +
                    "• ➕ **Добавить слово** — пополнить коллекцию\n" +
                    "• ✏️ **Редактировать** — изменить перевод слова\n" +
                    "• ❌ **Удалить слово** — убрать из словаря\n" +
                    "• ↩️ **Назад** — вернуться в меню\n\n" +
                    "Выберите действие:";

    private static final String ADD_WORD_INSTRUCTIONS =
            "📝 *Как добавить слово:*\n\n" +
                    "Просто отправьте мне слово на иностранном языке, а затем его перевод через пробел.\n" +
                    "А если хотите добавить фразу и перевод, то введите их через тире ('-') \n\n" +
                    "*Например:*\n" +
                    "`apple - яблоко`\n" +
                    "`looking for - искать (находиться в поиске)`";

    private static final String DELETE_INSTRUCTIONS =
            "🗑️ *Как удалить слово:*\n\n" +
                    "Просто отправьте мне слово на английском (без перевода), которое хотите удалить из словаря.\n\n" +
                    "*Например:*\n" +
                    "вы хотите удалить \"apple - яблоко\"\n" +
                    "введите: \"apple\"\n\n" +
                    "✨ *После удаления слово перестанет появляться в ваших тренировках!*";

    private static final String DELETE_INPUT_ERROR =
            "❌ *Неверный ввод слова!*\n\n" +
                    "Возможно, вы ошиблись в написании или использовали неверный формат.\n\n" +
                    "🔍 *Проверьте:*\n" +
                    "• Нет ли опечаток в слове?\n" +
                    "• Не добавили ли вы перевод?\n" +
                    "• Правильно ли указали язык слова?\n\n" +
                    "💫 *Попробуйте еще раз - я всегда готов помочь!*";

    private static final String EDIT_INSTRUCTIONS =
            "🔤 Редактирование перевода\n" +
                    "Чтобы отредактировать слово, введите его на английском языке " +
                    "в точности так, как оно указано в словаре. Изменить можно только " +
                    "его перевод на русский язык.";

    private static final String DELETE_CANCEL =
            "💫 *Удаление отменено*\n\n" +
                    "Слово осталось в вашем словаре и продолжит появляться в тренировках.\n\n" +
                    "✨ *Что дальше?*\n" +
                    "• 🗑️ Продолжить удаление других слов\n" +
                    "• 📚 Вернуться к изучению\n" +
                    "• 👀 Посмотреть словарь\n\n" +
                    "🌱 *Иногда сохранить - тоже важное решение!*";

    private static final String WORD_FOUND_NULL = "❌ Вы ничего не ввели";
    private static final String WORD_ERROR_FOUND = "❌ Ошибка при поиске слова.\n\n" +
            "Возможно вы ввели неправильно слово или его нет в словаре.";
    private static final String UNKNOWN_CLICK = "Неизвестная команда";

    public DictionaryCommand(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Получает userId по chatId для всех операций
     */
    private long getUserId(long chatId) throws SQLException {
        return dictionaryService.getUserIdByChatId(chatId);
    }

    /**
     * Показать словарь пользователя, стартовое сообщение
     * @param chatId идентификатор чата пользователя
     * @return текст сообщения со словарем
     */
    public String showDictionary(long chatId) {
        try {
            long userId = getUserId(chatId);
            List<Word> words = dictionaryService.getAllWords(userId);
            if (words.isEmpty()) {
                return DICTIONARY_EMPTY;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("✨ *Добро пожаловать в ваш личный словарь!* ✨\n\n")
                    .append("Здесь вы можете смотреть и пополнять свою уникальную коллекцию слов для изучения.\n\n")
                    .append("📚 *Ваш словарь* (").append(words.size()).append(" слов)\n\n");

            for (Word word : words) {
                sb.append(word.toString()).append("\n");
            }

            sb.append("\n🛠️ *Доступные действия:*\n\n")
                    .append("• ➕ **Добавить слово** — пополнить коллекцию\n")
                    .append("• ✏️ **Редактировать** — изменить слово или перевод\n")
                    .append("• ❌ **Удалить слово** — убрать из словаря\n")
                    .append("• ↩️ **Назад** — вернуться в меню\n\n")
                    .append("Выберите действие:");

            return sb.toString();
        } catch (SQLException e) {
            return "❌ Ошибка при загрузке словаря: " + e.getMessage();
        }
    }

    /**
     * Добавить слово в словарь
     * @param chatId идентификатор чата пользователя
     * @param englishWord английское слово
     * @param translation перевод слова
     * @return сообщение о результате добавления
     */
    public String addWord(long chatId, String englishWord, String translation) {
        try {
            long userId = getUserId(chatId);
            dictionaryService.addWord(userId, englishWord, translation, DEFAULT_PRIORITY);
            System.out.println("Слово добавлено в словарь");

            return "🔤 *Новое слово добавлено!*\n\n" +
                    "Слово: **" + englishWord + "**\n" +
                    "Перевод: **" + translation + "**\n\n"+
                    "✨ *Пополнить еще словарь?*";
        } catch (SQLException e) {
            return "❌ Ошибка при добавлении слова: " + e.getMessage();
        }
    }

    /**
     * Метод для возврата текста с запросом нового перевода
     * @param chatId идентификатор чата пользователя
     * @param englishWord английское слово для редактирования
     * @return сообщение с запросом нового перевода
     */
    public String getEditTranslationMessage(Long chatId, String englishWord) {
        try {
            long userId = getUserId(chatId);
            Word word = dictionaryService.getWordByEnglish(userId, englishWord);
            if (word == null) {
                return WORD_FOUND_NULL;
            }
            userStates.put(chatId, "waiting_edit_confirmation:" + word.getId());

            return "✏️ *Редактирование перевода*\n\n" +
                    "📝 Слово: **" + word.getEnglishWord()  + "**\n" +
                    "🎯 Перевод: **" + word.getTranslation() + "**\n\n" +
                    "💫 *Введите новый перевод:* 📝";

        } catch (SQLException e) {
            return WORD_ERROR_FOUND + ": " + e.getMessage();
        }
    }

    /**
     * Изменить перевод слова в словаре
     * @param chatId идентификатор чата пользователя
     * @param wordId идентификатор слова
     * @param newTranslation новый перевод
     * @return сообщение о результате обновления
     */
    public String updateTranslation(long chatId, int wordId, String newTranslation) {
        try {
            long userId = getUserId(chatId);
            Word existingWord = dictionaryService.getWordById(userId, wordId);
            if (existingWord == null) {
                return WORD_FOUND_NULL;
            }

            String englishWord = existingWord.getEnglishWord();
            String oldTranslation = existingWord.getTranslation();
            int priority = existingWord.getPriority();

            dictionaryService.updateWord(userId, wordId, englishWord, newTranslation, priority);
            System.out.println("Перевод слова обновлен в словаре");

            return "Отлично! Перевод успешно обновлён ✅\n\n" +
                    oldTranslation + " → " + newTranslation + "\n" +
                    "Слово сохранено в вашем словаре ✨";

        } catch (SQLException e) {
            return "❌ Ошибка при изменении перевода: " + e.getMessage();
        }
    }

    /**
     * Подтверждение на удаление
     * @param chatId идентификатор чата пользователя
     * @param englishWord английское слово для удаления
     * @return сообщение с подтверждением удаления
     */
    private String getDeleteConfirmation(long chatId, String englishWord) {
        if (englishWord == null || englishWord.trim().isEmpty()) {
            userStates.remove(chatId);
            return "❌ Пожалуйста, введите корректное слово";
        }

        String searchWord = englishWord.trim().toLowerCase();
        try {
            long userId = getUserId(chatId);
            Word word = dictionaryService.getWordByEnglish(userId, searchWord);

            if (word == null) {
                return DELETE_INPUT_ERROR;
            }
            // Сохраняем ID слова в состоянии для подтверждения
            userStates.put(chatId, "waiting_delete_confirmation:" + word.getId());
            System.out.println("Подтверждение удаления в словаре");

            // Получаем перевод ТОЛЬКО после проверки что word не null
            String translation = word.getTranslation();

            return "🗑️ *Подтвердите удаление*\n\n" +
                    "📝 Слово: **\"" + englishWord + "\"**\n" +
                    "🎯 Перевод: **\"" + translation + "\"**\n\n" +
                    "✨ *Это слово было частью вашего языкового пути!*\n" +
                    "❓ *Вы уверены, что хотите попрощаться с \"" + englishWord + "\"?*\n\n" +
                    "⚠️ *Напоминание:* после удаления слово исчезнет из всех ваших тренировок и больше не будет повторяться.\n\n" +
                    "💫 *Принимайте взвешенное решение!*";

        } catch (SQLException e) {
            userStates.remove(chatId);
            return WORD_ERROR_FOUND + ": " + e.getMessage();
        }
    }

    /**
     * Удалить слово из словаря
     * @param chatId идентификатор чата пользователя
     * @param wordId идентификатор слова
     * @return сообщение о результате удаления
     */
    public String deleteWord(long chatId, int wordId) {
        try {
            long userId = getUserId(chatId);
            Word word = dictionaryService.getWordById(userId, wordId);
            if (word == null) {
                return WORD_FOUND_NULL;
            }

            dictionaryService.deleteWord(userId, wordId);
            System.out.println("Удаление в словаре");
            return "✅ *Готово! Слово \"" + word.getEnglishWord() + "\" удалено*\n\n" +
                    "Теперь **\"" + word.getTranslation() + "\"** больше не будет появляться в вашем словаре" +
                    "и в ваших тренировках.\n\n";
        } catch (SQLException e) {
            return "❌ Ошибка при удалении слова: " + e.getMessage();
        }
    }

    /**
     * HandleButtonClick - метод обрабатывающий реакции, взятые с кнопок
     * @param callbackData данные callback с кнопки
     * @param chatId идентификатор чата пользователя
     * @return текст ответа на нажатие кнопки
     */
    public String handleButtonClick(String callbackData, long chatId) {
        switch (callbackData) {
            case "dictionary_button":
                return showDictionary(chatId);

            case "dictionary_add_button":
                userStates.put(chatId, "waiting_add_word");
                return ADD_WORD_INSTRUCTIONS;

            case "dictionary_edit_button":
                userStates.put(chatId, "waiting_edit_word");
                return EDIT_INSTRUCTIONS;

            case "dictionary_delete_button":
                userStates.put(chatId, "waiting_delete_word");
                return DELETE_INSTRUCTIONS;

            case "dictionary_add_yes_button":
                return ADD_WORD_INSTRUCTIONS;

            case "dictionary_add_no_button":
                return showDictionary(chatId);

            case "dictionary_delete_cancel_button":
                return DELETE_CANCEL;

            case "dictionary_delete_resume_button":
                userStates.put(chatId, "waiting_delete_word");
                return DELETE_INSTRUCTIONS;

            // Обработка входящих сообщений
            default:
                // Подтверждение удаления - УДАЛЯЕМ слово
                if (callbackData.startsWith("dictionary_delete_confirm_button")) {
                    System.out.println("Процесс удаления слова из словаря");
                    String currentState = userStates.get(chatId);
                    try {
                        int wordId = Integer.parseInt(currentState.split(":")[1]);
                        userStates.remove(chatId);
                        return deleteWord(chatId, wordId);
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка" + e.getMessage());
                        return "❌ Ошибка удаления: неверный формат ID слова в состоянии";
                    }
                }
                // Добавление в словарь
                else if (callbackData.startsWith("dictionary_add_button")) {
                    System.out.println("Процесс добавления слова из словаря");
                    try {
                        String[] parts = callbackData.substring("dictionary_add_button".length()).split("_");
                        if (parts.length >= 2) {
                            String englishWord = parts[0];
                            String translation = parts[1];
                            return addWord(chatId, englishWord, translation);
                        }
                    } catch (Exception e) {
                        return "❌ Неверный формат данных для добавления слова";
                    }
                }
                // Изменение перевода слова, подтверждение
                else if (callbackData.startsWith("dictionary_edit_confirm_button")) {
                    System.out.println("Процесс согласия на редактирование слова из словаря");
                    try {
                        String data = callbackData.substring("dictionary_edit_confirm_button".length());
                        String[] parts = data.split("_to_");
                        if (parts.length == 2) {
                            int wordId = Integer.parseInt(parts[0]);
                            String newTranslation = parts[1];
                            return updateTranslation(chatId, wordId, newTranslation);
                        }
                    } catch (NumberFormatException e) {
                        return "❌ Неверный формат данных для редактирования";
                    }
                }
                return UNKNOWN_CLICK;
        }
    }

    /**
     * Обработка текстовых сообщений в словаре
     * @param text текст сообщения пользователя
     * @param chatId идентификатор чата пользователя
     * @return текст ответа на текстовую команду
     */
    public String handleTextCommand(String text, long chatId) {
        // Обработка статуса добавления слова
        if ("waiting_add_word".equals(userStates.get(chatId))) {
            // Для фразовых выражений
            if (text.contains(" - ")) {
                String[] parts = text.split(" - ");
                if (parts.length == 2) {
                    return addWord(chatId, parts[0].trim(), parts[1].trim());
                }
            }
            String trimmedText = text.trim();
            String[] parts = trimmedText.split("\\s+"); // разделяем по одному или более пробелам

            if (parts.length == 2) {
                return addWord(chatId, parts[0], parts[1]);
            } else if (parts.length > 2) {
                // Если больше двух слов, берем первое слово как английское, остальное как перевод
                String englishWord = parts[0];
                String translation = trimmedText.substring(trimmedText.indexOf(' ') + 1);
                return addWord(chatId, englishWord, translation);
            }
        }
        // Запуск метода через статус удаления с полученным словом
        if ("waiting_delete_word".equals(userStates.get(chatId))) {
            System.out.println("Введено слово для удаления в словаре");
            return getDeleteConfirmation(chatId, text.trim());
        }
        // Запуск метода через статус редактирования с полученным словом (первым английском)
        if ("waiting_edit_word".equals(userStates.get(chatId))){
            System.out.println("Введено первое слово для изменения перевода в словаре");
            return getEditTranslationMessage(chatId, text.trim());
        }

        // Ожидаем новый перевод для редактирования
        if (userStates.get(chatId) != null && userStates.get(chatId).startsWith("waiting_edit_confirmation:")) {
            try {
                int wordId = Integer.parseInt(userStates.get(chatId).split(":")[1]);
                userStates.remove(chatId); // очищаем состояние
                System.out.println("Введено второе слово для изменения перевода в словаре");
                return updateTranslation(chatId, wordId, text.trim());
            } catch (NumberFormatException e) {
                userStates.remove(chatId);
                return "❌ Ошибка при обновлении перевода";
            }
        }
        return "❌ Неправильный ввод или команда";
    }

    /**
     * Сбрасывает состояния словаря для пользователя
     * @param chatId идентификатор чата пользователя
     */
    public void resetUserState(long chatId) {
        userStates.remove(chatId);
        System.out.println("[DictionaryCommand] Состояния словаря сброшены для chatId: " + chatId);
    }
}