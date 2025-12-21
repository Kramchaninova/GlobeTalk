package org.example.Tokens;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Класс для загрузки и хранения токена бота из ДС из файла.
 * Обеспечивает безопасное чтение токена из текстового файла.
 *
 */
public class TokenDiscord {
    private String token = "";

    /**
     * Возвращает текущий токен бота
     */
    public String get() {
        return token;
    }

    /**
     * Загружает токен бота из файла TOKEN_DISCORD.txt в ресурсах
     */
    public void load() {
        String fileName = "src/main/resources/TOKEN_DISCORD.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            token = reader.readLine();
            System.out.println("Токен загружен ДС");
        } catch (IOException e) {
            System.err.println("Ошибка загрузки токена для Discord: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
