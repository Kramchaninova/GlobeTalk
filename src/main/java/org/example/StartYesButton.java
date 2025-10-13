package org.example;

import java.io.IOException;

/** StartYesButton - класс, который хранит запрос (PROMT),
 * подгружает api token openrouter,
 * вызывает метод отправки нашего запроса непосредственно ии
 */


public class StartYesButton {
    //наш  шедевро запрос
    private static final String PROMPT= "Напиши тест на 9 вопросов на проверку английского языка: 3 вопроса на уровень c1-c2, 3 вопроса на уровень b1-b2," +
            "3 вопроса на уровень a1-a2. так же навешай баллы за ответы: на уровень c - 3 балла, на уровень b  - 2 балла," +
            "на уровень a - 1 балла. так же нужно относительно уровней делать в разброс задания, так же на каждый вопрос" +
            "должно быть 4 варианта ответов. " +
            "пример как должен выглядеть вопрос: " +
            "первая строка состоит из самого из номера вопроса и в скобочках подписан балл за него, например 2, без всяких point" +
            "вторая строка состоит из вопроса или задания" +
            "следующие строки для вариантов ответа, записаные как Большая буква с точкой и вариант ответа" +
            "последняя строка: правильный ответ оформляется как 'Answer:' и через пробел буква правильного ответа( то есть буква)." +
            " также сделай вопросы отсносительно сложности в разброс ";

    /**
     * Метод для генерации теста и сохранение в txt файл
     */
    public String getGeneratedTest() {
        try {
            // загружаем API ключ через Token_Openrouter, ОЧЕНЬ ВАЖНО: мы работаем не напрямую с гпт
            //Опен Роутер - это "лпатформа-посредник"
            Token_Openrouter token_openrouter = new Token_Openrouter();
            token_openrouter.load();

            String apiKey = token_openrouter.get();

            //оносительно бесполезная вещь
            if (apiKey == null || apiKey.isEmpty()) {
                return "API ключ не найден";
            }

            //создаем экземпляр класса OpenRouterClient и закидываем тут наш токен опенроутера
            OpenRouterClient openRouterClient = new OpenRouterClient(apiKey);
            return openRouterClient.sendRequest(PROMPT);

        } catch (IOException | InterruptedException e) {
            return "ошибка при генерации теста: " + e.getMessage();
        }
    }
}
