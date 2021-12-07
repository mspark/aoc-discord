package de.mspark.aoc.parsing;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mspark.aoc.parsing.Entry.Completions;


public class CompletionsDeserializer implements JsonDeserializer<Completions> {

    @Override
    public Completions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jObject = json.getAsJsonObject();
        Instant nowUtc = Instant.now();
        ZoneId zone = ZoneId.of("America/New_York");
        int day = ZonedDateTime.ofInstant(nowUtc, zone).getDayOfMonth();
        if (jObject.has("" + day)) {
            JsonObject completion = jObject.get("" + day).getAsJsonObject();
            int stageComplete = 0;
            String latestCompletion = "";
            if (completion.has("1")) {
                latestCompletion = completion.get("1").getAsJsonObject().get("get_star_ts").getAsString();
                stageComplete++;
                if (completion.has("2")) {
                    latestCompletion = completion.get("2").getAsJsonObject().get("get_star_ts").getAsString();
                    stageComplete++;
                }            
            }
            return new Completions(day, stageComplete, latestCompletion);
        } else {
            return new Completions(day, 0, "");
        }
    }

}
