package de.mspark.aoc.leaderboard;

import java.awt.Color;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.MiscUtils;
import de.mspark.aoc.verficiation.DiscordNameResolver;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class PrivateLeaderboardService extends LeaderboardService {    
    private AocConfig config;
    private DiscordNameResolver mapper;
    
    public PrivateLeaderboardService(AocConfig config, DiscordNameResolver mapper) {
        super(config);
        this.config = config;
        this.mapper = mapper;
    }

    public MessageEmbed retrieveLeaderboardEmbed() {
        return createEmbedOnLeaderboard(lb -> new EmbedBuilder()
                .setTitle("Advent Of Code: Top10 Leaderboard")
                .setDescription(lb.generateTopTenEntrysAsString())
                .appendDescription("\n\n Du willst mitmachen? Klick [hier](https://adventofcode.com/2021/leaderboard/private) und nutze: `%s`".formatted(config.inviteCode()))
                .setTimestamp(Instant.now())
                .setColor(new Color(231, 42, 100)) // #e72a64
                .build());
    }
    
    public MessageEmbed retrieveFullLeaderboardEmbed() {
        return createEmbedOnLeaderboard(
            lb -> new EmbedBuilder().setTitle("Advent of Code private leaderboard")
                .setDescription(lb.generateAllEntrysAsString())
                .setTimestamp((Instant.now()))
                .build());
    }
    
    public MessageEmbed retrieveDailyLeaderboardEmbed() {
        int day = MiscUtils.getAocDay();
        return createEmbedOnLeaderboard(
            lb -> new EmbedBuilder().setTitle("Completions for day " + day)
                .setDescription(lb.generateDailyCompletionAsString())
                .build());
    }
    
    private MessageEmbed createEmbedOnLeaderboard(Function<LocalLeaderboard, MessageEmbed> runnable) {
        var entrys =  callAndParseLeaderboard(config.privateLeaderboardId());
        try {
            var lb = new LocalLeaderboard(entrys.orElseThrow(), mapper);
            return runnable.apply(lb);
        } catch (NoSuchElementException e) {
            return new EmbedBuilder().setDescription("Currently the leaderboard is unavailable. Contact an administrator").build();
        }
    }
    
    @Override
    public String getAocLeaderboardId() {
        return config.privateLeaderboardId();
    }
}
