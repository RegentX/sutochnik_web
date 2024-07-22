package org.example.sutochnikweb.services;

import org.example.sutochnikweb.models.Action;
import org.springframework.stereotype.Service;

@Service
public class TimeService {
    public String convertMillisToTime(int millis){
        int seconds = millis / 1000;

        // Извлекаем количество часов
        int hours = seconds / 3600;

        // Извлекаем оставшиеся минуты
        int minutes = (seconds % 3600) / 60;

        // Извлекаем оставшиеся секунды
        long remainingSeconds = seconds % 60;

        // Форматируем в строку вида "HH:mm:ss"
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
}
