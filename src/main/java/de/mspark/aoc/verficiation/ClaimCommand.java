package de.mspark.aoc.verficiation;

import java.util.List;

import de.mspark.aoc.verficiation.exceptions.AlreadyVerifiedExcpetion;
import de.mspark.jdaw.Command;
import de.mspark.jdaw.CommandProperties;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.config.JDAWConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@CommandProperties(trigger = "claim", description = "Claims your AOC account and connects it with discord", executableWihtoutArgs = true)
public class ClaimCommand extends Command {
    private VerificationTaskManager verifier;
    
    public ClaimCommand(JDAWConfig conf, JDAManager jdas, VerificationTaskManager verifier) {
        super(conf, jdas);
        this.verifier = verifier;
    }
    
    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        try {
            verify(msg);            
        } catch (AlreadyVerifiedExcpetion e) {
            msg.reply("Your already claimed your AOC account.").submit();
        }
    }

    private void verify(Message msg) {
        var optInvite = this.verifier.registerVerifyAction(msg.getAuthor().getId());
        if (optInvite.isPresent()) {
            msg.reply("Look at your DMs!").submit();
            msg.getAuthor().openPrivateChannel().complete()
                .sendMessage("Please join this leaderbord `" + optInvite.get() + "`. You have 5 minutes to do that. When you're done, I'll notify you again")
                .submit();
        } else {
            msg.reply("There is an ongoing verfification process with a different user. Due to limitation of remote API, only one person can do a verification at once. Retry it later")
                .submit();
        }
    }
    
    @Override
    protected MessageEmbed fullHelpPage() {
        return new EmbedBuilder().setDescription("You can claim an AOC Account by proving that you're the owner of an Account. "
                + "Afterwards it is connected with your Servermembership here and your Discord Name appears in the leaderboard instead of your AOC Name. ")
                .build();
    }
}
