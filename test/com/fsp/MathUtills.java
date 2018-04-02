package com.fsp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

/**
 * Created by lucky on 2017/7/10.
 */
public class MathUtills {
    public static void main(String[] args) {
        checkInSector();

        Instant instant = Instant.now();
        System.out.println(instant);
        System.out.println(instant.getNano());

        LocalDateTime ldt = LocalDateTime.now();
        System.out.println(ldt);
        System.out.println(ldt.getDayOfYear());
        System.out.println(ldt.get(ChronoField.DAY_OF_WEEK));
    }

    public static boolean checkInSector() {
        int x1 = 10;
        int y1 = 10;

        int x2 = 30;
        int y2 = 30;

        System.out.println(Math.atan2(20,20));
        System.out.println(Math.toDegrees(Math.atan2(20,20)));
        return true;
    }
}
