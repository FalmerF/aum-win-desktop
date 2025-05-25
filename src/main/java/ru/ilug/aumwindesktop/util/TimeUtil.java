package ru.ilug.aumwindesktop.util;

import java.time.Duration;

public class TimeUtil {

    public static String formatSeconds(long totalSeconds) {
        Duration duration = Duration.ofSeconds(totalSeconds);
        long hours = duration.toHours();
        int minutes = (int) (duration.toMinutes() % 60);
        int seconds = (int) (duration.getSeconds() % 60);

        return String.format("%sч. %sм. %sс.", hours, minutes, seconds);
    }

}
