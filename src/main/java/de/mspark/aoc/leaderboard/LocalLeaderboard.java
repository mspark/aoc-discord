package de.mspark.aoc.leaderboard;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.mspark.aoc.MiscUtils;
import de.mspark.aoc.parsing.Entry;
import de.mspark.aoc.verficiation.DiscordNameResolver;

class LocalLeaderboard {
        private List<Entry> sortedEntrys;
        private DiscordNameResolver mapper;
        
        static final String[] ranks = {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":keycap_ten:"};
        static final String[] STAIRS = {":crown:", ":star2:", ":star:"};
        private int rank = 0;
        private int count = 0;
        
        LocalLeaderboard(List<Entry> entrys, DiscordNameResolver mapper) {
            this.sortedEntrys = entrys.stream().sorted().toList();
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
        
        String generateTopTenEntrysAsString() {
            return sortedEntrys.stream()
                .map(this::getTopTenEntry)
                .flatMap(Optional::stream)
                .reduce((a,b) -> a + "\n" + b)
                .orElse("No user in private leaderboard");
        }
        
        String generateAllEntrysAsString() {
            return sortedEntrys.stream()
                    .map(this::getLeaderboardEntry)
                    .map(s -> getAndIncreaseRank() + ". " + s)
                    .collect(Collectors.joining("\n"));
        }
        
        String generateDailyCompletionAsString() {
            int day = MiscUtils.getAocDay();
            return sortedEntrys.stream()
                .filter(e -> e.stagesCompleteForDay(day).orElse(0) > 0)
                .sorted((a, b) -> a.compareWithDailyScore(b, day))                
                .map(e -> dailyCompletionEntry(e))
                .reduce((a,b) -> a + "\n" + b)  
                .orElse("No user in private leaderboard");
        }

        private int getAndIncreaseRank() {
            rank++;
            return rank;
        }

        private String dailyCompletionEntry(Entry leaderboardMember) {
            int day = MiscUtils.getAocDay();
            return getAndIncreaseRank() + ". " 
                    + getName(leaderboardMember) + " completed **" + leaderboardMember.stagesCompleteForDay(day).get() + "**"
                    + " *(latest " + leaderboardMember.completionTimeFormatted() + ")*";
        }
        
        private String getLeaderboardEntry(Entry leaderboardMember) {
            String name = mapper.getVerifiedName(leaderboardMember.id()).orElse(leaderboardMember.name());
            return name + " **[ " + leaderboardMember.localScore() + " ]**"; 
        }
        
        private String getName(Entry entry) {
            return mapper.getVerifiedName(entry.id()).orElse(entry.name());
        }
    }