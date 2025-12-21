package org.example.Tokens;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Класс для загрузки и хранения токена бота из TG из файла.
 * Обеспечивает безопасное чтение токена из текстового файла.
 *
 */
public class Token {
    private String token = "";

    /**
     * Возвращает текущий токен бота
     */
    public String get() {
        return token;
    }

    /**
     * Загружает токен бота из файла TOKEN.txt в ресурсах
     */
    public void load() {
        String fileName = "src/main/resources/TOKEN_TELEGRAM.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            token = reader.readLine();
            System.out.println("Токен загружен TG");
        } catch (IOException e) {
            System.err.println("Ошибка загрузки токена для Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
