package de.mspark.aoc.leaderboard;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import de.mspark.aoc.AocConfig;
import de.mspark.jdaw.Command;
import de.mspark.jdaw.CommandProperties;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.config.JDAWConfig;
import de.mspark.jdaw.guilds.GuildConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
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
    
    public LeaderboardCommand(JDAWConfig conf, GuildConfigService gc, JDAManager jdas, PrivateLeaderboardService lbService, AocConfig config) {
        super(conf, gc, jdas);
        this.lbService = lbService;
        this.roomCode = "`" + config.inviteCode() + "`";
    }

    @Override
    public void doActionOnCmd(Message msg, List<String> cmdArguments) {
        if (cmdArguments.size() > 0) {
            switch (cmdArguments.get(0)) {
                case "full" -> msg.getChannel().sendMessage(lbService.retrieveFullLeaderboardEmbed()).submit();
                case "day" -> actionSendDailyCompletions(msg);
                default -> msg.getChannel().sendMessage("Invalid arguments").submit();
            }
        } else {
             msg.getChannel()
                 .sendMessage(lbService.retrieveLeaderboardEmbed()).submit()
                 .thenAccept(m -> cacheNewMessageDeleteOld(m));
        }
    }

    private void actionSendDailyCompletions(Message msg) {
        msg.getChannel()
            .sendMessage(lbService.retrieveDailyLeaderboardEmbed()).submit()
            .thenAccept(m -> cacheNewMessageDeleteOld(m));
    }

    @Override
    protected MessageEmbed fullHelpPage() {
        return new EmbedBuilder()
                .setDescription(getShortDescription() + "\n Room code: " + roomCode + "\n The leaderboard is sent daily. "
                        + "If you manually ask for it, the sent message will be automatically removed after a specific time to keep the channel history clean.")
                .addField("full", "With this parameter you see the whole leaderboard and not just the ten best", false)
                .addField("daily", "Shows the number of completed stages of all member which at least completed on stage on the current day", false)
                .build();
    }
    
   private void cacheNewMessageDeleteOld(Message msg) {
       if (!msg.isFromType(ChannelType.PRIVATE)) {
           String oldLeaderboardMessageId = sendLeaderboardMessages.get(msg.getChannel().getId());
           if (oldLeaderboardMessageId != null) {
               msg.getChannel().deleteMessageById(oldLeaderboardMessageId).submit();
           }
           sendLeaderboardMessages.put(msg.getChannel().getId(), msg.getId());
       }
   }
}
