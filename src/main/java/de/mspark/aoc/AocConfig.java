package de.mspark.aoc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "aoc")
public record AocConfig(
        String dailyChannelId,
        String privateLeaderboardId, 
        String verificationLeaderboardId,
        String inviteCode, 
        String session) {
}
