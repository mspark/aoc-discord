package de.mspark.aoc;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.mspark.jdaw.cmdapi.TextCommand;
import de.mspark.jdaw.help.HelpConfig;
import de.mspark.jdaw.startup.JDAManager;
import de.mspark.jdaw.startup.JdawConfig;
import de.mspark.jdaw.startup.JdawInstance;
import de.mspark.jdaw.startup.JdawInstanceBuilder;

@ConfigurationProperties(prefix = "bot")
record BotConfig(String defaultPrefix, String[] apiTokens) implements JdawConfig {}

@Component
class OwnHelpConfig implements HelpConfig {
    
    @Override
    public String botDescription() {
        return "";
    }
    
    @Override
    public String botName() {
        return "Advent of Code leaderboard Bot";
    }
    
}

@Configuration
@ConfigurationPropertiesScan
class BeanConfig {
    
    @Bean
    JdawInstanceBuilder builder(JdawConfig config) {
        return new JdawInstanceBuilder(config);
    }
    
    @Bean
    JdawInstance instance(JdawInstanceBuilder builder, List<TextCommand> cmds, HelpConfig helpConfig) {
        return builder.addCommand(cmds.toArray(TextCommand[]::new)).enableHelpCommand(helpConfig).buildJdawInstance();
    }
    
    
    @Bean
    JDAManager manager(JdawInstance instance) {
        return instance.getCurrentState().jdaManager();
    }
}