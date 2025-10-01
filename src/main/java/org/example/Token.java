package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Класс для загрузки и хранения токена бота из файла.
 * Обеспечивает безопасное чтение токена из текстового файла.
 *
 */
public class Token {
    String token = "";

    /**
     * Возвращает текущий токен бота
     */
    public String get() {
        return token;
    }

    /**
     * Загружает токен бота из файла tocken.txt в ресурсах
     */
    public void load() {
        String fileName = "src/main/resources/token.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            token = reader.readLine();
            System.out.println("Токен загружен");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
