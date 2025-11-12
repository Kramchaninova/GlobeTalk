package org.example.Tokens;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Класс для загрузки и хранения токена OpenRouter (для подключении ии) и из файла.
 * Обеспечивает безопасное чтение токена из текстового файла.
 *
 */
public class TokenOpenRouter {
    private String tokenOpenRouter = "";

    /**
     * Возвращает текущий токен бота
     */
    public String get() {
        return tokenOpenRouter;
    }

    /**
     * Загружает токен бота из файла TOKEN.txt в ресурсах
     */
    public void load() {
        String fileName = "src/main/resources/TOKEN_OPENROUTER.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            tokenOpenRouter = reader.readLine();
            System.out.println("Токен Openrouter загружен");
        } catch (IOException e) {
            System.err.println("Ошибка загрузки токена OpenRouter: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
