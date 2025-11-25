package org.example.Interface;

/**
 * Интерфейс для управления рассылками сообщений
 */
public interface DistributionService {

    /**
     * Запускает рассылку сообщений с указанными параметрами
     * @param initialDelay начальная задержка в секундах
     * @param period период между рассылками в секундах
     */
    void startDistribution(int initialDelay, int period);

    /**
     * Останавливает рассылку сообщений
     */
    void stopDistribution();

}