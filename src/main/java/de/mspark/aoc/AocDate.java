package de.mspark.aoc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class AocDate {
    
    public static int getAocDay() {
    	var date = getDateTimeAocZone();
    	if (date.getMonthValue() == 12) {
            return date.getDayOfMonth();            
        } throw new RuntimeException("AOC Bot should be disabled. Its not december.");
    }
    
    public static LocalDateTime getDateTimeAocZone() {
    	ZoneId zone = ZoneId.of("America/New_York");
        return Instant.now().atZone(zone).toLocalDateTime();
    }
    
    public static ZoneId getTimeZone() {
    	return ZoneId.systemDefault();
    }
}
