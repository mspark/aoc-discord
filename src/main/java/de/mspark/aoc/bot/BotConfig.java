package de.mspark.aoc.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import de.mspark.jdaw.config.JDAWConfig;

@ConstructorBinding
@ConfigurationProperties(prefix = "bot")
public record BotConfig(String prefix, String[] apiTokens) implements JDAWConfig {}
