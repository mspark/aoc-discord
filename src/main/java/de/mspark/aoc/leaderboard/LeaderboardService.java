package de.mspark.aoc.leaderboard;

import static de.mspark.aoc.AocDate.getDateTimeAocZone;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.json.GsonJsonParser;

import com.google.gson.GsonBuilder;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.parsing.CompletionsDeserializer;
import de.mspark.aoc.parsing.Entry;
import de.mspark.aoc.parsing.Entry.Completions;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class LeaderboardService {
    private AocConfig config;
    
    public LeaderboardService(AocConfig config) {
        super();
        this.config = config;
    }
    
    public Optional<List<Entry>> retrieveLeaderboard() {
        return callAndParseLeaderboard(getAocLeaderboardId());
    }

    public void removeUserFromLeaderboard(String aocMemberId) {
        removeUserFromLeaderboard(aocMemberId, getAocLeaderboardId());
    }
    
    public abstract String getAocLeaderboardId();

    protected Optional<List<Entry>> callAndParseLeaderboard(String leaderboardId) {
        return makeAocHttpCall("https://adventofcode.com/" + getDateTimeAocZone().getYear()+ "/leaderboard/private/view/" + leaderboardId + ".json")
                .map(LeaderboardService::parseBodyJson);
    }

    protected void removeUserFromLeaderboard(String aocUserId, String leaderboardId) {
        makeAocHttpCall("https://adventofcode.com/" + getDateTimeAocZone().getYear() + "/leaderboard/private/part/%s/%s".formatted(leaderboardId, aocUserId));
    }
    
    protected static List<Entry> parseBodyJson(String body) {
        // TODO do this with a type adapter :)
        @SuppressWarnings("unchecked")
        Map<String, Object> membersList = (Map<String, Object>) new GsonJsonParser().parseMap(body).get("members");
        var gson = new GsonBuilder().registerTypeAdapter(Completions.class, new CompletionsDeserializer()).create();
        return membersList.values().stream()
                .map(gson::toJson)
                .map(entry -> gson.fromJson(entry, Entry.class))
                .toList();
    }
    
    /**
     * Send a HTTP call to a specified URL with an AOC session.
     * 
     * @param url Desired AOC URL
     * @return HTTP Body from response. Is empty when a failure occure during the http call.
     */
    public Optional<String> makeAocHttpCall(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", "session=" + config.session())
                .build();
        Response response;
        try {
            response = new OkHttpClient().newCall(request).execute();
            return Optional.of(response.body().string());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
