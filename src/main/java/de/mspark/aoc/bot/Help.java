package de.mspark.aoc.bot;

import java.util.List;

import de.mspark.jdaw.Command;
import de.mspark.jdaw.JDAManager;
import de.mspark.jdaw.config.JDAWConfig;
import de.mspark.jdaw.help.EnableHelpCommand;
import de.mspark.jdaw.help.GlobalHelpCommand;

@EnableHelpCommand
public class Help extends GlobalHelpCommand {

    public Help(JDAWConfig conf, JDAManager jdas, List<Command> allLoadedCmds) {
        super(conf, jdas, allLoadedCmds);
    }

    @Override
    public String botDescription() {
        return "";
    }
    
    @Override
    public String botName() {
        return "Advent of Code leaderboard Bot";
    }

}
