package org.example.Data;

/**
 * Ответ бота для отправки сообщения пользователю.
 * Содержит ID чата, текст и тип клавиатуры.
 */
public class BotResponse {
    private final long chatId;
    private final String text;
    private final String keyboardType;

    /**
     * Создает ответ без клавиатуры.
     * @param chatId ID чата получателя
     * @param text текст сообщения
     */
    public BotResponse(long chatId, String text) {
        this(chatId, text, null);
    }

    /**
     * Создает ответ с клавиатурой.
     * @param chatId ID чата получателя
     * @param text текст сообщения
     * @param keyboardType тип клавиатуры
     */
    public BotResponse(long chatId, String text, String keyboardType) {
        this.chatId = chatId;
        this.text = text;
        this.keyboardType = keyboardType;
    }

    /** @return ID чата получателя */
    public long getChatId() {
        return chatId;
    }

    /** @return текст сообщения */
    public String getText() {
        return text;
    }

    /** @return тип клавиатуры */
    public String getKeyboardType() {
        return keyboardType;
    }

    /** @return true если требуется клавиатура */
    public boolean hasKeyboard() {
        return keyboardType != null && !keyboardType.isEmpty();
    }

    /** @return true если ответ валиден (типо что текст не равен нулю или пустутоте) для отправки */
    public boolean isValid() {
        return text != null && !text.isEmpty();
    }
}