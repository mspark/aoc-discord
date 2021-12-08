package de.mspark.aoc;

import java.time.LocalDate;

public final class MiscUtils {
    
    public static int getAocDay() {
        var date = LocalDate.now();
        if (date.getMonthValue() == 12) {
            return LocalDate.now().getDayOfMonth();            
        } throw new RuntimeException("AOC Bot should be disabled. Its not december.");
    }
}
