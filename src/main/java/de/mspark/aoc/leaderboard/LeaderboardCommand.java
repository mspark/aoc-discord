package de.mspark.aoc.leaderboard;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Component;

import de.mspark.aoc.AocConfig;
import de.mspark.jdaw.cmdapi.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Component
public class LeaderboardCommand extends TextCommand {

    private final PrivateLeaderboardService lbService;
    private final String roomCode;
    
    /**
     * Key is the channel id where the leaderboard is sent
     * Value  is the messages which has been sent by the bot.
     */
    private final Map<String, String> sendLeaderboardMessages = new HashedMap<>(); 
    
    public LeaderboardCommand(PrivateLeaderboardService lbService, AocConfig config) {
        this.lbService = lbService;
        this.roomCode = "`" + config.inviteCode() + "`";
    }

    @Override
    public void onTrigger(Message msg, List<String> cmdArguments) {
        if (cmdArguments.size() > 0) {
            try {
                menu(msg, cmdArguments);
            } catch (NoLeaderboardEntriesException e) {
                msg.getChannel().sendMessage("Currently the leaderboard is unavailable. Contact an administrator").submit();
            }
        } else {
             msg.getChannel()
                 .sendMessageEmbeds(lbService.retrieveLeaderboardEmbed()).submit()
                 .thenAccept(m -> cacheNewMessageDeleteOld(m));
        }
    }

    private void menu(Message msg, List<String> cmdArguments) {
        switch (cmdArguments.get(0)) {
        case "full" -> msg.getChannel().sendMessageEmbeds(lbService.retrieveFullLeaderboardEmbed()).submit();
        case "day" -> actionSendDailyCompletions(msg);
        default -> actionSendYearlyLeaderboard(msg, cmdArguments.get(0));
        }
    }
    
    private void actionSendYearlyLeaderboard(Message msg, String argument) {
        try {
            int number = Integer.parseInt(argument);
            if (number > 2019) {
                msg.getChannel().sendMessageEmbeds(lbService.retrieveYearLeaderboardEmbed(number)).submit();
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            msg.getChannel().sendMessage("Invalid arguments").submit();
        }
    }

    private void actionSendDailyCompletions(Message msg) {
        msg.getChannel()
            .sendMessageEmbeds(lbService.retrieveDailyLeaderboardEmbed()).submit()
            .thenAccept(m -> cacheNewMessageDeleteOld(m));
    }

    @Override
    public MessageEmbed commandHelpPage() {
        return new EmbedBuilder()
                .setDescription(description() + "\n Room code: " + roomCode + "\n The leaderboard is sent daily. "
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

    @Override
    public String trigger() {
        return "leaderboard";
    }
    
    @Override
    public String description() {
        return "Shows the leaderboard of a private room";
    }
    
    @Override
    public boolean executableWihtoutArgs() {
        return true;
    }
    
    @Override
    public boolean privateChatAllowed() {
        return true;
    }
    
    @Override
    public String[] aliases() {
        return new String[] {"lb", "board"};
    }

}
