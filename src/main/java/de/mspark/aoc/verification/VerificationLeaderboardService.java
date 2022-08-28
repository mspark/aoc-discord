package de.mspark.aoc.verification;

import org.springframework.stereotype.Service;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.leaderboard.LeaderboardService;

@Service
public class VerificationLeaderboardService extends LeaderboardService {
    private AocConfig config;

    public VerificationLeaderboardService(AocConfig config, DiscordNameResolver mapper) {
        super(config);
        this.config = config;
    }

    @Override
    public String getAocLeaderboardId() {
        return config.verificationLeaderboardId();
    }

}
