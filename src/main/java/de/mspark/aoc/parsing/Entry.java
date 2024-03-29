package de.mspark.aoc.parsing;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import de.mspark.aoc.AocDate;

public class Entry implements Comparable<Entry> {
    
    // Currently this record is needed for parsing. When a type adapter is available for the whole entry, its attributes can be moved to the entry class
    public static record Completions(int day, int stagesComplete, String completionTime) {
    }

    private int id;
    private String name;
    private int local_score;
    private Completions completion_day_level;

    public String name() {
        if (name == null || name.isBlank()) {
            return "Anonymer Teilnehmer #" + id;
        } else {
            return name;
        }
    }

    public int localScore() {
        return local_score;
    }

    @Override
    public int compareTo(Entry arg0) {
        return arg0.local_score - this.local_score;
    }
    
    public int compareWithDailyScore(Entry arg0, int currentAocDay) {
        int diff = arg0.stagesCompleteForDay(currentAocDay).orElse(0) - this.stagesCompleteForDay(currentAocDay).orElse(0);
        if (diff == 0) {
        	// when nothing was solved so far, use the latest possible time (now) for ordering
            var fallbackTime = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();
            diff = this.latestStageCompletionTime().orElse(fallbackTime)
            		.compareTo(arg0.latestStageCompletionTime().orElse(fallbackTime));
        }
        return diff;
    }
    
    public String id() {
        return id + "";
    }
    
    public String getCompletionMessage() {
        int complete = completion_day_level.stagesComplete;
        if (complete != 0) {
            String word = complete == 1 ? "erste" : "zweite";
            return "**%s** hat die %s Aufgabe von Tag %s um **%s** Uhr gelöst".formatted(name, word, completion_day_level.day, completionTimeFormatted());
        } else {
            return "%s hat noch keine Aufgabe an %s gelöst".formatted(name, completion_day_level.day);
        }
    }
    
    /**
     * Updates the cache information about the completed stages of the user. Only the latest update is saved. The new entry overrides the old information.
     * 
     * @param e
     */
    public void updateCompletion(Entry e) {
        this.completion_day_level = e.completion_day_level;
    }
    
    /**
     * Gives the number of completed stages for one day. Currently only one (typically the latest one) is saved in the cache. 
     * When older dates are requested, the return value contains nothing.
     * 
     * @param day
     * @return
     */
    public Optional<Integer> stagesCompleteForDay(int day) {
        Optional<Integer> stagesComplete = Optional.empty();
        if (completion_day_level.day == day) {
            stagesComplete =  Optional.of(completion_day_level.stagesComplete);
        } 
        return stagesComplete;
    }
    
    public Optional<LocalDateTime> latestStageCompletionTime() {
        String tsString = completion_day_level.completionTime;
        if (tsString != null) {
            long ts = (long) Double.parseDouble(completion_day_level.completionTime) * 1000;
            var zone = AocDate.getTimeZone();
            var time = Instant.ofEpochMilli(ts).atZone(zone).toLocalDateTime();
            return Optional.of(time);
        }
        return Optional.empty();
    }
    
    public String completionTimeFormatted() {
        var dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return latestStageCompletionTime().map(dateFormat::format).orElse("Not solved");
    }
}