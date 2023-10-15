package dev.discordnet.gatekeeper;

import com.edgedb.driver.EdgeDBClient;
import com.edgedb.driver.EdgeDBClientConfig;
import com.edgedb.driver.EdgeDBConnection;
import com.edgedb.driver.exceptions.EdgeDBException;
import com.edgedb.driver.namingstrategies.NamingStrategy;
import dev.discordnet.gatekeeper.discord.CommandRegister;
import dev.discordnet.gatekeeper.discord.MCVerifyCommand;
import dev.discordnet.gatekeeper.events.PlayerJoined;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GateKeeper extends JavaPlugin {
    private EdgeDBClient edgedbClient;
    private JDA jda;

    @Override
    public void onEnable() {
        try {
            edgedbClient = new EdgeDBClient(
                    EdgeDBConnection.parse((String) null, null, false)
                    , EdgeDBClientConfig.builder()
                    .withNamingStrategy(NamingStrategy.snakeCase())
                    .build()
            );
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e, () -> "Failed to enable GateKeeper");
            return;
        }

        final var botToken = System.getenv("BOT_TOKEN");

        if(botToken == null) {
            getLogger().log(Level.SEVERE, () -> "Missing the BOT_TOKEN environment variable");
            return;
        }

        jda = JDABuilder.createDefault(botToken)
                .addEventListeners(
                        new MCVerifyCommand(this),
                        new CommandRegister(this)
                ).build();

        getServer().getPluginManager().registerEvents(new PlayerJoined(this), this);
        getLogger().log(Level.INFO, "Gatekeeper registers B)");
    }

    public EdgeDBClient getEdgeDB() {
        return this.edgedbClient;
    }
    public JDA getJDA() {
        return this.jda;
    }

}
