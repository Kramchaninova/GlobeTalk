package org.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * OpenRouterClient - класс работы с ии.
 */

public class OpenRouterClient {
    private final String apiKey;

    public OpenRouterClient(String apiKey) {
        this.apiKey = apiKey;
    }


    /**
     * отправка запроса через OpenRouter.
     *
     * @param userPrompt - наш запрос, который мы задали в StartYesButton
     * @return - возвращаем ответ на запрос в виде строки
     * @throws IOException          - если проблемы с сетью или с вводом/выводом
     * @throws InterruptedException - если поток был вызван ожидания ответа
     */

    public String sendRequest(String userPrompt) throws IOException, InterruptedException {
        String requestBody = createRequestBody(userPrompt); //создание запрос в формате json, чуть позже объясню почему так (не забудь спросить)
        HttpRequest request = createHttpRequest(requestBody); //создание запроса HTTP с определенными заголовками и параметрами

        HttpClient client = HttpClient.newHttpClient(); //создание клиента дял HTTP запроса
        //отпарвка запроса и ответ
        // BodyHandlers.ofString() - указывает что тело ответа нужно преобразовать в строку
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //обрабатываем и получаем рез
        return processResponse(response);
    }


    /**
     * createRequestBody - метод для создания тела HTTP запроса в формате json
     *
     * @param userPrompt - наш запрос
     * @return - возвращает json строку с телом запроса
     */
    private String createRequestBody(String userPrompt) {
        // String.format - подставляет значение userPrompt в шаблон
        // replace("\"", "\\\"") - экранирует кавычки в промпте чтобы не сломать JSON
        return String.format("""
                    {
                      "model": "gpt-3.5-turbo",
                      "messages": [
                        {"role": "system", "content": "You are a helpful assistant."},
                        {"role": "user", "content": "%s"}
                      ]
                    }
                """, userPrompt.replace("\"", "\\\""));
    }

    /**
     * createHttpRequest - метод для создания HTTP запроса
     *
     * @param requestBody - тело в формате json (предыдущий метод)
     * @return настроенный объект HttpRequest
     */

    private HttpRequest createHttpRequest(String requestBody) {

        // builder для пошагового создания запроса
        return HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions")) //ссылка endpoint API OpenRouter
                .header("Content-Type", "application/json") // заголовок указывающий тип (json)
                .header("Authorization", "Bearer " + apiKey) //заголовок авторизация Bearer и токеном
                .header("HTTP-Referer", "https://example.com") //заголовок источник запроса указывает типо так required by OpenRouter
                .header("X-Title", "Java Test App")//заголовок название приложения
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)) //указываем метод post и передеаем тело запроса
                .build();
    }

    /**
     * processResponse - метод обработки HTTp ответ от APi
     *
     * @param response - ответ от сервера
     * @return - возвращаем текст от ии
     * @throws JSONException - ошибка парсинга json
     */

    private String processResponse(HttpResponse<String> response) throws JSONException {
        // вывод статус кода, 200 - все рабоатет
        //если 400 какаято то впн значит отваллися
        System.out.println("Status code: " + response.statusCode());

        //парсим JSON ответ в объект для удобного доступа к полям
        JSONObject json = new JSONObject(response.body());

        // проверка статус кода HTTP, то что написано выше
        if (response.statusCode() != 200) {
            String errorMessage = "HTTP Error: " + response.statusCode();

            // ивлекаем сообщение об ошибке
            if (json.has("error")) {
                // если есть объект error, берем сообщение из него
                JSONObject error = json.getJSONObject("error");
                errorMessage += " - " + error.getString("message");
                if (error.has("type")) {
                    errorMessage += " (Type: " + error.getString("type") + ")";
                }
            } else if (json.has("message")) {
                // альтернативный вариант - поле message
                errorMessage += " - " + json.getString("message");
            }
            //бросаем исключение с информацией об ошибке
            throw new RuntimeException(errorMessage);
        }

        // проверяем наличие поля "choices" (массив) в ответе
        if (!json.has("choices")) {
            throw new RuntimeException("Error: 'choices' field not found in response. Available keys: " + json.keySet());
        }

        // извлекаем текст ответа ии из структуры JSON:
        // json -> choices (массив) -> первый элемент -> message -> content
        return json.getJSONArray("choices") // получаем массив choices
                .getJSONObject(0) //забираем первый элемент массива
                .getJSONObject("message") // получаем объект message
                .getString("content") // извлекаем текст ответа
                .trim(); // убираем лишние пробелы по краям
    }
}
