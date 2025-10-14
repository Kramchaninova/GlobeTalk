package org.example;

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
        String fileName = "src/main/resources/TOKEN.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            token = reader.readLine();
            System.out.println("Токен загружен TG");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
