package de.mspark.aoc.verification;

import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import de.mspark.jdaw.startup.JDAManager;

@Component
public class DiscordNameResolver {

    private JDAManager jdas;
    private PersistentVerificationData verificationData;
    
    public DiscordNameResolver(@Lazy JDAManager jdas, PersistentVerificationData verificationData) {
        this.jdas = jdas;
        this.verificationData = verificationData;
    }

    public Optional<String> getVerifiedName(String aocMemberId) {
        var discordId = verificationData.get(aocMemberId);
        if (discordId != null) {
            var mention = this.jdas.getNextJDA().retrieveUserById(discordId).complete().getAsMention();
            return Optional.of(mention);
        } else {
            return Optional.empty();
        }
    }
}
