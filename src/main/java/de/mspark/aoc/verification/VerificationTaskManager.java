package de.mspark.aoc.verification;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.AocDate;
import de.mspark.aoc.leaderboard.LeaderboardService;
import de.mspark.aoc.verification.exceptions.AlreadyVerifiedExcpetion;
import de.mspark.aoc.verification.exceptions.MaxRetriesReachedException;
import de.mspark.jdaw.startup.JDAManager;

@Component
@ThreadSafe
public class VerificationTaskManager {
    private final LeaderboardService lbService;
    private final JDAManager jdas;
    private final PersistentVerificationData verificationData;
    private final ConcurrentLinkedQueue<VerificationTask> verifyTasks= new ConcurrentLinkedQueue<>();
    
    public VerificationTaskManager(VerificationLeaderboardService lbs, @Lazy JDAManager jda, AocConfig config, PersistentVerificationData verificationData) {
        this.lbService = lbs;
        this.jdas = jda;
        this.verificationData = verificationData;
    }
    
    public Optional<String> registerVerifyAction(String discordUserId) {
        boolean userIsVerified = verificationData.values().stream()
            .filter(id -> id.equals(discordUserId))
            .findAny()
            .isPresent();
        if (userIsVerified) {
            throw new AlreadyVerifiedExcpetion();
        } else if (verifyTasks.isEmpty()) {
            /*
             * Currently it is not possible to create multiple leaderboards at once so limit the number of concurrent verifications
             * to only once at a time. 
             */
            verifyTasks.add(new VerificationTask(discordUserId, lbService));
            kickAllUserFromLeaderboard(); // just for safety
            return Optional.of(regenerateInviteCode());
        } else {
            return Optional.empty();
        }
    }
    
    @Scheduled(fixedDelay = 60000)
    public void checkVerification() {
        for (VerificationTask vtask : verifyTasks) {
            try {
                if (vtask.verifyUser()) {
                    String verifiedDiscordId = vtask.getVerifiedDiscordUserId();
                    verificationData.put(vtask.getAocId(), verifiedDiscordId);
                    jdas.getNextJDA().openPrivateChannelById(verifiedDiscordId).complete()
                        .sendMessage("Verified!").submit();
                    verifyTasks.remove(vtask);
                    kickAllUserFromLeaderboard();
                }
            } catch (MaxRetriesReachedException e) {
                System.out.println("Max timeout reached");
                verifyTasks.remove(vtask);
            }
        }
    }

    private String regenerateInviteCode() {
       Pattern pattern = Pattern.compile("<code>(\\w+-\\w+)</code>", Pattern.CASE_INSENSITIVE);
       return lbService.makeAocHttpCall("https://adventofcode.com/%s/leaderboard/private/reset".formatted(AocDate.getDateTimeAocZone().getYear()))
            .map(pattern::matcher)
            .filter(Matcher::find)
            .map(match -> match.group(1))
            .orElseThrow();
        
    }

    private void kickAllUserFromLeaderboard() {
        lbService.retrieveLeaderboard()
            .orElseGet(Collections::emptyList).stream()
            .forEach(e -> lbService.removeUserFromLeaderboard(e.id()));
    }
}
