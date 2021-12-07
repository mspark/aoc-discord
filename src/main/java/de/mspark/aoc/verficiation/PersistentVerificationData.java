package de.mspark.aoc.verficiation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Repository
public class PersistentVerificationData extends ConcurrentHashMap<String, String> {
    private static final long serialVersionUID = -9079634355774745104L;
    public static final File JSON_FILE = new File("verifications.json");
    private static final Gson GSON = new GsonBuilder().create();
    
    public PersistentVerificationData() {
        super();
        if (!JSON_FILE.exists()) {
            saveToFile();
        }
        this.putAll(readFromFile());
    }
    
    @SuppressWarnings("unchecked")
    private static synchronized Map<String, String> readFromFile() {
        String jsonContent;
        try {
            jsonContent = Files.readAllLines(JSON_FILE.toPath()).stream().collect(Collectors.joining());
            return GSON.fromJson(jsonContent, Map.class); // dont use this.getClass, result in an infinite loop
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error("Verification savefile was not loaded. Operating without it!");
        }
      
    }
    
    public synchronized void saveToFile() {
        String jsonContent = GSON.toJson(this);
        try {
            Files.writeString(JSON_FILE.toPath(), jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String put(String key, String value) {
        var smthm = super.put(key, value);
        saveToFile();
        return smthm;
    }
    
}
