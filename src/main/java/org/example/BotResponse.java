package org.example;

public class BotResponse {
    private final long chatId;
    private final String text;
    private final String keyboardType;

    public BotResponse(long chatId, String text, String keyboardType) {
        this.chatId = chatId;
        this.text = text;
        this.keyboardType = keyboardType;
    }

    public long getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }

    public String getKeyboardType() {
        return keyboardType;
    }
}
