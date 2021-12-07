package de.mspark.aoc.verficiation;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.mspark.aoc.AocConfig;
import de.mspark.aoc.leaderboard.LeaderboardService;
import de.mspark.aoc.verficiation.exceptions.AlreadyVerifiedExcpetion;
import de.mspark.aoc.verficiation.exceptions.MaxRetriesReachedException;
import de.mspark.jdaw.JDAManager;

@Component
@ThreadSafe
public class VerificationTaskManager {
    private final LeaderboardService lbService;
    private final JDAManager jdas;
    private final PersistentVerificationData verificationData;
    private final ConcurrentLinkedQueue<VerificationTask> verifyTasks= new ConcurrentLinkedQueue<>();
    
    public VerificationTaskManager(VerificationLeaderboardService lbs, JDAManager jda, AocConfig config, PersistentVerificationData verificationData) {
        this.lbService = lbs;
        this.jdas = jda;
        this.verificationData = verificationData;
    }
    
    public Optional<String> registerVerifyAction(String discordUserId) {
        /*
         * Currently it is not possible to create multiple leaderboards at once so limit the number of concurrent verifications
         * to only once at a time.  
         */
        boolean userIsVerified = verificationData.values().stream()
            .filter(id -> id.equals(discordUserId))
            .findAny()
            .isPresent();
        if (userIsVerified) {
            throw new AlreadyVerifiedExcpetion();
        } else if (verifyTasks.isEmpty()) {
            verifyTasks.add(new VerificationTask(discordUserId, lbService));
            kickAllUserFromLeaderboard(lbService.getAocLeaderboardId());
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
                }
            } catch (MaxRetriesReachedException e) {
                System.out.println("Max timeout reached");
                verifyTasks.remove(vtask);
            }
        }
    }

    private String regenerateInviteCode() {
       Pattern pattern = Pattern.compile("<code>(\\w+-\\w+)</code>", Pattern.CASE_INSENSITIVE);
       return lbService.makeAocHttpCallWithBody("https://adventofcode.com/2021/leaderboard/private/reset")
            .map(body -> pattern.matcher(body))
            .filter(match -> match.find())
            .map(match -> match.group(1))
            .orElseThrow();
        
    }

    private void kickAllUserFromLeaderboard(String lbId) {
        lbService.retrieveLeaderboard()
            .orElseGet(Collections::emptyList).stream()
            .forEach(e -> lbService.removeUserFromLeaderboard(e.id()));
    }
}
