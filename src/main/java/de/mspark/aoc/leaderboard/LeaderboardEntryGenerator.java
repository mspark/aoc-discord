package de.mspark.aoc.leaderboard;

import java.util.Optional;

import de.mspark.aoc.AocDate;
import de.mspark.aoc.parsing.Entry;
import de.mspark.aoc.verficiation.DiscordNameResolver;

class LeaderboardEntryGenerator {
    static final String[] ranks = {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":keycap_ten:"};
    static final String[] STAIRS = {":crown:", ":star2:", ":star:"};
    private final DiscordNameResolver mapper;
    private int rank = 0;
    private int count = 0;
    
    public LeaderboardEntryGenerator(DiscordNameResolver mapper) {
        super();
        this.mapper = mapper;
    }

    Optional<String> getTopTenEntry(Entry leaderboardMember) {
        int rank = getAndIncreaseRank();
        count++;
        if (count >= 11) {
            return Optional.empty();
        } else {
            String rankStr = (rank <= 10 ? ranks[rank - 1]: "");
            String extra = rank <= 3 ? STAIRS[rank - 1]: "";
            String text = (extra != "" ? extra : rankStr) + " " + getLeaderboardEntry(leaderboardMember);           
            if (count == 3) { // Additional line to seperate the top three
                text += "\n";
            }
            return Optional.of(text);
        }
    }
    String dailyCompletionEntry(Entry leaderboardMember) {
        int day = AocDate.getAocDay();
        return getAndIncreaseRank() + ". " 
                + getName(leaderboardMember) + " completed **" + leaderboardMember.stagesCompleteForDay(day).get() + "**"
                + " *(latest " + leaderboardMember.completionTimeFormatted() + ")*";
    }

    String getRankedLeaderboardEntry(Entry leaderboardMember) {
        return getAndIncreaseRank() + ". " + getLeaderboardEntry(leaderboardMember);
    }
    
    private int getAndIncreaseRank() {
        rank++;
        return rank;
    }
    
    private String getLeaderboardEntry(Entry leaderboardMember) {
        String name = mapper.getVerifiedName(leaderboardMember.id()).orElse(leaderboardMember.name());
        return name + " **[ " + leaderboardMember.localScore() + " ]**"; 
    }
    
    private String getName(Entry entry) {
        return mapper.getVerifiedName(entry.id()).orElse(entry.name());
    }
}
