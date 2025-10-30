package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTestHandler {

    private final TestHandler testHandler = new TestHandler();

    private String testText;

    /**
     * Проверка генерации теста для последующих тестов
     */
    @BeforeEach
    void testGenerateTest() {
        testText = """
                1 (1 points)
                What is the capital of France?
                A. London
                B. Paris
                C. Madrid
                D. Rome
                Answer: B
                
                2 (2 points)
                Which word is a verb?
                A. Apple
                B. Run
                C. Table
                D. House
                Answer: B
                
                3 (3 points)
                Choose the correct sentence:
                A. He go to school.
                B. He goes to school.
                C. He going to school.
                D. He gone to school.
                Answer: B
                """;

        testHandler.generateTest(12345L, testText);
    }

    /**
     * Проверка разницы между правильным и неправильным ответом
     * Она работает через TestHendler, те есть ответ от ии в виде вопроса и ответа
     */
    @Test
    void testCorrectVsIncorrectScoreDifference() {
        String simpleTest = """
            1 (10 points)
            Simple question?
            A. Wrong
            B. Correct
            C. Wrong
            D. Wrong
            Answer: B
            """;

        // ТЕСТ 1: правильный ответ
        testHandler.generateTest(11111L, simpleTest);
        //псевдопользователь нажимает на кнопку, в данном случае правильную
        //и в correctFinalResponse начисляются баллы
        String correctFinalResponse = testHandler.handleAnswer("B_button", 11111L);

        // ТЕСТ 2: неправильный ответ
        testHandler.generateTest(22222L, simpleTest);
        //тоже самое, но баллы не начиляются
        String incorrectFinalResponse = testHandler.handleAnswer("A_button", 22222L);

        // для правильного ответа ожидаем 10 баллов (указано в сообщении)
        //тут специально стоит проверка assertTrue, потому что мы проверяем подстроку, а именно колво баллов
        Assertions.assertTrue(correctFinalResponse.contains("10 из 10"),
                "При правильном ответе должны быть все баллы. Получено: " + correctFinalResponse);

        // для неправильного ответа ожидаем 0 баллов
        Assertions.assertTrue(incorrectFinalResponse.contains("0 из 10"),
                "При неправильном ответе баллы не должны начисляться. Получено: " + incorrectFinalResponse);
    }

    /**
     * Проверка на вывод результатов после теста на уровень A1-A2
     */
    @Test
    void testFinalAnswerMinimalLevel() {
        String customTestText = """
                1 (6 points)
                Test question?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        testHandler.generateTest(12345L, customTestText);
        String finalResponse = testHandler.handleAnswer("A_button", 12345L);

        String expectedResponse = "🎉 *Тест завершён!* 🎉\n\n" +

                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 0 из 6 возможных\n" +
                "📈 Уровень владения языком: A1-A2 (Начальный)\n\n" +

                "✨ **Отличная работа!** ✨\n\n";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }

    /**
     * Проверка на вывод результатов после теста на уровень C1-C2.
     */
    @Test
    void testFinalAnswerMaximalLevel() {
        String customTestText = """
                1 (14 points)
                Test question?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        testHandler.generateTest(12345L, customTestText);
        String finalResponse = testHandler.handleAnswer("B_button", 12345L);

        String expectedResponse = "🎉 *Тест завершён!* 🎉\n\n" +

                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 14 из 14 возможных\n" +
                "📈 Уровень владения языком: C1-C2 (Продвинутый)\n\n" +

                "✨ **Отличная работа!** ✨\n\n";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }

    /**
     * Проверка подсчета баллов при правильных и неправильных ответах, и в конце вывод на уровень B1-B2
     */
    @Test
    void testScoreCalculation() {
        String testText = """
                1 (6 points)
                Question 1?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                2 (6 points)
                Question 2?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                
                3 (6 points)
                Question 3?
                A. Wrong
                B. Correct
                C. Wrong
                D. Wrong
                Answer: B
                """;

        testHandler.generateTest(99999L, testText); // ДРУГОЙ chatId чтобы не конфликтовать
        testHandler.handleAnswer("B_button", 99999L);//вкрный ответ (+6 баллов)
        testHandler.handleAnswer("A_button", 99999L); //неверный ответ
        String finalResponse = testHandler.handleAnswer("B_button", 99999L);//верный ответ

        String expectedResponse = "🎉 *Тест завершён!* 🎉\n\n" +

                "📊 **Результаты тестирования:**\n" +
                "🏆 Набрано баллов: 12 из 18 возможных\n" +
                "📈 Уровень владения языком: B1-B2 (Средний)\n\n" +

                "✨ **Отличная работа!** ✨\n\n";

        Assertions.assertEquals(expectedResponse, finalResponse);
    }
}
