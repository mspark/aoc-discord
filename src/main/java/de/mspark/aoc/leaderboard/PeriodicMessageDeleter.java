package de.mspark.aoc.leaderboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.mspark.aoc.AocConfig;
import de.mspark.jdaw.JDAManager;

@Component
@EnableScheduling
public class PeriodicMessageDeleter {
    private List<String> deleteMessages = new ArrayList<>();

    @Autowired
    private JDAManager jdas;

    @Autowired
    private AocConfig config;
    
    @Scheduled(cron = "*/60 */2 * * * *")
    public void deleteAllMessages() {
        deleteMessages.forEach(m -> jdas.getNextJDA().getTextChannelById(config.dailyChannelId()).deleteMessageById(m).submit());
    }
    
    public void addMessages(String... msg) {
        deleteMessages.addAll(List.of(msg));
    }
}
