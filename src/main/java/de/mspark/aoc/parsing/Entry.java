package de.mspark.aoc.parsing;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Entry implements Comparable<Entry> {
    
    // Currently this record is needed for parsing. When a type adapter is available for the whole entry, its attributes can be moved to the entry class
    public static record Completions(int day, int stagesComplete, String completionTime) {
        public String completionTime() {
            double db = Double.parseDouble(completionTime) * 1000;
            var ts = new Timestamp((long) db);
            var dateFormat = new SimpleDateFormat("kk:mm:ss");
            return dateFormat.format(ts);
        }
    }

    private String id;
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
    
    public String id() {
        return id;
    }
    
    public String getCompletionMessage() {
        int complete = completion_day_level.stagesComplete;
        if (complete != 0) {
            String word = complete == 1 ? "erste" : "zweite";
            return "**%s** hat die %s Aufgabe von Tag %s um **%s** Uhr gelöst".formatted(name, word, completion_day_level.day, completion_day_level.completionTime());
        } else {
            return "%s hat noch keine Aufgabe an %s gelöst".formatted(name, completion_day_level.day);
        }
    }
    
    public void updateCompletion(Entry e) {
        this.completion_day_level = e.completion_day_level;
    }
    
    public int stagesComplete() {
        return completion_day_level.stagesComplete;
    }
}