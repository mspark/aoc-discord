package de.mspark.aoc.leaderboard;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.MiscUtils;
import de.mspark.aoc.parsing.Entry;
import de.mspark.jdaw.JDAManager;

@Component
@EnableScheduling
public class LeaderboardWriter {

    public PrivateLeaderboardService lbService;
    
    @Autowired
    public JDAManager jda;
    
    @Autowired
    public AocConfig config;

    private Map<String, Entry> savedEntrys;
    
    public LeaderboardWriter(PrivateLeaderboardService lbService) {
        this.lbService = lbService;
        this.savedEntrys = lbService.retrieveLeaderboard().get().stream().collect(Collectors.toMap(e -> e.id(), Function.identity()));
    }
    
    @Scheduled(cron = "0 0 6 1-25 12 *")
    public void writeDailyMessage() {
        int day = MiscUtils.getAocDay();
        String dailyText = ":sparkles: Die Aufgaben für den **" + day + ". Tag** von Advent-Of-Code können nun gelöst werden! :sparkles:";
        var txtChannel = jda.getNextJDA().getTextChannelById(config.dailyChannelId());
        txtChannel.sendMessage(dailyText).submit();
        if (day > 1) {
            txtChannel.sendMessage(lbService.retrieveLeaderboardEmbed()).submit();
        }
    }
    
    @Scheduled(cron = "*/60 */2 * * * *")
    public void checkAndWriteForTaskCompletion() {
        var currentLb = lbService.retrieveLeaderboard();
        int day = MiscUtils.getAocDay();
        currentLb.get().forEach(current -> {
            var old = savedEntrys.get(current.id());
            int oldStagesCompleted = old.stagesCompleteForDay(day).orElse(0);
            int actualStagesCompleted = current.stagesCompleteForDay(day).orElse(0);
            if (oldStagesCompleted < actualStagesCompleted) {
                jda.getNextJDA().getTextChannelById(config.dailyChannelId()).sendMessage(current.getCompletionMessage()).submit();
                old.updateCompletion(current);
            }
        });
    }
    
}
