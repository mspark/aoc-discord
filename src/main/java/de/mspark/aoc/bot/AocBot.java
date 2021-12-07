package de.mspark.aoc.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@ComponentScan({ "de.mspark.jdaw", "de.mspark.aoc" })
@ConfigurationPropertiesScan({"de.mspark.aoc"})
public class AocBot {
    
    public static void main(String[] args) {
        SpringApplication.run(AocBot.class, args);
    }
}
