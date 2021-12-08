package de.mspark.aoc.leaderboard;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import de.mspark.aoc.AocConfig;
import de.mspark.jdaw.Command;
import de.mspark.jdaw.CommandProperties;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.config.JDAWConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@CommandProperties(trigger = "leaderboard", 
    aliases = {"lb", "board"},
    description = "Shows the leaderboard of a private room", 
    helpPage = true,
    executableWihtoutArgs = true,
    privateChatAllowed = true)
public class LeaderboardCommand extends Command {

    private final PrivateLeaderboardService lbService;
    private final String roomCode;
    
    /**
     * Key is the channel id where the leaderboard is sent
     * Value  is the messages which has been sent by the bot.
     */
    private final Map<String, String> sendLeaderboardMessages = new HashedMap<>(); 
    
    public LeaderboardCommand(JDAWConfig conf, JDAManager jdas, PrivateLeaderboardService lbService, AocConfig config) {
        super(conf, jdas, false);
        this.lbService = lbService;
        this.roomCode = "`" + config.inviteCode() + "`";
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        if (cmdArguments.size() > 0) {
            if (cmdArguments.get(0).equals("full")) {
                msg.getChannel().sendMessage(lbService.retrieveFullLeaderboardEmbed()).submit();
            } else {
                msg.getChannel().sendMessage("Invalid arguments").submit();
            }
        } else {
            var textMessageFuture = msg.getChannel().sendMessage(lbService.retrieveLeaderboardEmbed()).submit();
            textMessageFuture.thenAccept(textMsg -> cacheNewMessageDeleteOld(textMsg));
        }
//        msg.getAuthor().openPrivateChannel().complete().sendMessage(lbService.retrieveLeaderboardEmbed()).submit();
    }

    @Override
    protected MessageEmbed fullHelpPage() {
        return new EmbedBuilder()
                .setDescription(getShortDescription() + "\n Room code: " + roomCode + "\n The leaderboard is sent daily. "
                + "If you manually ask for it, the sent message will be automatically removed after a specific time to keep the channel history clean.")
                .addField("full", "With this parameter you see the whole leaderboard and not just the ten best", false)
                .build();
    }
    
   private void cacheNewMessageDeleteOld(Message msg) {
       String oldLeaderboardMessageId = sendLeaderboardMessages.get(msg.getChannel().getId());
       if (oldLeaderboardMessageId != null) {
           msg.getChannel().deleteMessageById(oldLeaderboardMessageId).submit();
       }
       sendLeaderboardMessages.put(msg.getChannel().getId(), msg.getId());
   }
}
