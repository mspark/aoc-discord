package de.mspark.aoc.verification;

import java.util.List;
import java.util.Optional;

import de.mspark.aoc.leaderboard.LeaderboardService;
import de.mspark.aoc.parsing.Entry;
import de.mspark.aoc.verification.exceptions.MaxRetriesReachedException;

class VerificationTask {
    private static final int MAX_RETRIES = 5;
    private final LeaderboardService lbService;
    private final boolean isVerified = false;
    private final String verifyDiscordId;
    private Entry lbEntry;
    private int pastRetries = 0;

    public VerificationTask(String verfiyDiscordId, LeaderboardService lbService) {
        this.verifyDiscordId = verfiyDiscordId;
        this.lbService = lbService;
    }

    public boolean verifyUser() throws MaxRetriesReachedException {
        if (pastRetries < MAX_RETRIES) {
            if (!isVerified) {
                var optionalLeaderboardEntrys = lbService.retrieveLeaderboard();
                optionalLeaderboardEntrys.flatMap(this::findAnyUserExceptOwn).ifPresent(e -> this.lbEntry = e);
                pastRetries++;
                return lbEntry != null;
            } else {
                return true;
            }
        }
        throw new MaxRetriesReachedException();
    }

    private Optional<Entry> findAnyUserExceptOwn(List<Entry> lbEntrys) {
        String ownUser = lbService.getAocLeaderboardId(); // id is always the user itself (except for global
                                                          // leaderboard)
        var opt = lbEntrys.stream().filter(e -> !e.id().equals(ownUser)).findFirst();
        return opt;
    }

    public Optional<String> getVerifiedAocMemberId() {
        if (lbEntry != null) {
            return Optional.of(lbEntry.id());
        }
        return Optional.empty();
    }

    public String getLeaderboardUsedForVerification() {
        return lbService.getAocLeaderboardId();
    }

    public String getAocId() {
        return lbEntry.id();
    }

    public String getVerifiedDiscordUserId() {
        return verifyDiscordId;
    }
}