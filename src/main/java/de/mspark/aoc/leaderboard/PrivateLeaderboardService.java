package de.mspark.aoc.leaderboard;

import java.awt.Color;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.AocDate;
import de.mspark.aoc.parsing.Entry;
import de.mspark.aoc.verification.DiscordNameResolver;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

class NoLeaderboardEntriesException extends RuntimeException {

    private static final long serialVersionUID = 6025691405224765206L;

}

@Service
public class PrivateLeaderboardService extends LeaderboardService {    
    private final AocConfig config;
    private final DiscordNameResolver mapper;
    
    public PrivateLeaderboardService(AocConfig config, DiscordNameResolver mapper) {
        super(config);
        this.config = config;
        this.mapper = mapper;
    }

    public MessageEmbed retrieveLeaderboardEmbed() {
        var entries = sortedLeaderboardEntries();
        var leaderboardText = this.generateTopTenEntrysAsString(entries);
        return new EmbedBuilder()
            .setTitle("Advent Of Code: Top10 Leaderboard")
            .setDescription(leaderboardText)
            .appendDescription("\n\n Du willst mitmachen? Klick [hier](https://adventofcode.com/%s/leaderboard/private) und nutze: `%s`".formatted(AocDate.getDateTimeAocZone().getYear(), config.inviteCode()))
            .setTimestamp(Instant.now())
            .setColor(new Color(231, 42, 100)) // #e72a64
            .build();
    }
    
    public MessageEmbed retrieveFullLeaderboardEmbed() {
        var entries =  sortedLeaderboardEntries();
        var leaderboardText = this.generateAllEntrysAsString(entries);
        return new EmbedBuilder().setTitle("Advent of Code private leaderboard")
                .setDescription(leaderboardText)
                .setTimestamp((Instant.now()))
                .build();
    }

    private Stream<Entry> sortedLeaderboardEntries() {
        return callAndParseLeaderboard(config.privateLeaderboardId()).orElseThrow(NoLeaderboardEntriesException::new).stream().sorted();
    }
    
    public MessageEmbed retrieveDailyLeaderboardEmbed() {
        int day = AocDate.getAocDay();
        var entries =  sortedLeaderboardEntries();
        var leaderboardText = generateDailyCompletionAsString(entries);
        return new EmbedBuilder().setTitle("Completions for day " + day)
            .setDescription(leaderboardText)
            .build();
    }
    
    public MessageEmbed retrieveYearLeaderboardEmbed(int year) {
        var entries =  callAndParseLeaderboard(config.privateLeaderboardId(), year).orElseThrow(NoLeaderboardEntriesException::new).stream().sorted();
        return new EmbedBuilder().setTitle("Advent of Code private leaderboard for the **year " + year + "**")
            .setDescription(this.generateAllEntrysAsString(entries))
            .build();
    }
    
    private String generateTopTenEntrysAsString(Stream<Entry> entries) {
        var creator = new LeaderboardEntryGenerator(mapper);
        return entries.map(creator::getTopTenEntry)
            .flatMap(Optional::stream)
            .reduce((a,b) -> a + "\n" + b)
            .orElse("No user in private leaderboard");
    }
    
    private String generateAllEntrysAsString(Stream<Entry> entries) {
        var creator = new LeaderboardEntryGenerator(mapper);
        return entries.map(creator::getRankedLeaderboardEntry)
                .collect(Collectors.joining("\n"));
    }
    
    private String generateDailyCompletionAsString(Stream<Entry> entries) {
        var creator = new LeaderboardEntryGenerator(null);
        int day = AocDate.getAocDay();
        return entries.filter(e -> e.stagesCompleteForDay(day).orElse(0) > 0)
            .sorted((a, b) -> a.compareWithDailyScore(b, day))                
            .map(creator::dailyCompletionEntry)
            .reduce((a,b) -> a + "\n" + b)  
            .orElse("No user in private leaderboard");
    }

    @Override
    public String getAocLeaderboardId() {
        return config.privateLeaderboardId();
    }
}
