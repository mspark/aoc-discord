package de.mspark.aoc.leaderboard;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.AocDate;
import de.mspark.aoc.parsing.Entry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class PrivateLeaderboardService extends LeaderboardService {    
    private final AocConfig config;
    private final LeaderboardEntryGenerator creator;
    
    public PrivateLeaderboardService(AocConfig config, LeaderboardEntryGenerator entryGenerator) {
        super(config);
        this.config = config;
        this.creator = entryGenerator;
    }

    public MessageEmbed retrieveLeaderboardEmbed() {
        return generateEmbedWithSortedLeadeboardEntrys(
            sortedList -> new EmbedBuilder()
                .setTitle("Advent Of Code: Top10 Leaderboard")
                .setDescription(this.generateTopTenEntrysAsString(sortedList))
                .appendDescription("\n\n Du willst mitmachen? Klick [hier](https://adventofcode.com/2021/leaderboard/private) und nutze: `%s`".formatted(config.inviteCode()))
                .setTimestamp(Instant.now())
                .setColor(new Color(231, 42, 100)) // #e72a64
                .build()
            );
    }
    
    public MessageEmbed retrieveFullLeaderboardEmbed() {
        return generateEmbedWithSortedLeadeboardEntrys(
            sortedList -> new EmbedBuilder().setTitle("Advent of Code private leaderboard")
                .setDescription(this.generateAllEntrysAsString(sortedList))
                .setTimestamp((Instant.now()))
                .build()
            );
    }
    
    public MessageEmbed retrieveDailyLeaderboardEmbed() {
        int day = AocDate.getAocDay();
        return generateEmbedWithSortedLeadeboardEntrys(
            sortedList -> new EmbedBuilder().setTitle("Completions for day " + day)
                .setDescription(this.generateDailyCompletionAsString(sortedList))
                .build()
            );
    }
    
    private MessageEmbed generateEmbedWithSortedLeadeboardEntrys(Function<List<Entry>, MessageEmbed> runnable) {
        var entrys =  callAndParseLeaderboard(config.privateLeaderboardId());
        try {
            return runnable.apply(
                    entrys.orElseThrow().stream().sorted().toList()
                );
        } catch (NoSuchElementException e) {
            return new EmbedBuilder().setDescription("Currently the leaderboard is unavailable. Contact an administrator").build();
        }
    }
    
    @Override
    public String getAocLeaderboardId() {
        return config.privateLeaderboardId();
    }
    
    private String generateTopTenEntrysAsString(List<Entry> sortedEntrys) {
        return sortedEntrys.stream()
            .map(creator::getTopTenEntry)
            .flatMap(Optional::stream)
            .reduce((a,b) -> a + "\n" + b)
            .orElse("No user in private leaderboard");
    }
    
    private String generateAllEntrysAsString(List<Entry> sortedEntrys) {
        return sortedEntrys.stream()
                .map(creator::getRankedLeaderboardEntry)
                .collect(Collectors.joining("\n"));
    }
    
    private String generateDailyCompletionAsString(List<Entry> sortedEntrys) {
        int day = AocDate.getAocDay();
        return sortedEntrys.stream()
            .filter(e -> e.stagesCompleteForDay(day).orElse(0) > 0)
            .sorted((a, b) -> a.compareWithDailyScore(b, day))                
            .map(creator::dailyCompletionEntry)
            .reduce((a,b) -> a + "\n" + b)  
            .orElse("No user in private leaderboard");
    }
}
