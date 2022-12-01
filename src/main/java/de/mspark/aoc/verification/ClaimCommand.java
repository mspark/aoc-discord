package de.mspark.aoc.verification;

import java.util.List;

import org.springframework.stereotype.Component;

import de.mspark.aoc.verification.exceptions.AlreadyVerifiedExcpetion;
import de.mspark.jdaw.cmdapi.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Component
public class ClaimCommand extends TextCommand {
    private VerificationTaskManager verifier;
    
    public ClaimCommand(VerificationTaskManager verifier) {
        this.verifier = verifier;
    }
    
    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        try {
            verify(msg);            
        } catch (AlreadyVerifiedExcpetion e) {
            msg.reply("You already claimed your AOC account.").submit();
        }
    }

    private void verify(Message msg) {
        var personalInviteCode = this.verifier.registerVerifyAction(msg.getAuthor().getId());
        if (personalInviteCode.isPresent()) {
            msg.getAuthor().openPrivateChannel().submit()
                .thenAccept(pchat -> pchat.sendMessage(
                    "Please join this leaderboard `%s`. You have 5 minutes to do that. When you're done, I'll notify you again"
                            .formatted(personalInviteCode.get()))
                    .submit()
                );
            msg.reply("Look at your DMs!").submit();
        } else {
            msg.reply("There is an ongoing verfification process with a different user. Due to limitation of remote API, only one person can do a verification at once. Retry it later")
                .submit();
        }
    }
    
    @Override
    public MessageEmbed commandHelpPage() {
        return new EmbedBuilder()
                .setDescription("You can claim an AOC Account by proving that you're the owner of an Account. "
                        + "Afterwards it is connected with your Servermembership here and your Discord Name appears in the leaderboard instead of your AOC Name. ")
                .build();
    }

    @Override
    public String trigger() {
        return "claim";
    }

    @Override
    public String description() {
        return "Claims your AOC account and connects it with discord";
    }
    
    @Override
    public boolean privateChatAllowed() {
        return true;
    }
    
    @Override
    public boolean executableWihtoutArgs() {
        return true;
    }
}
