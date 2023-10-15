package dev.discordnet.gatekeeper.discord;

import dev.discordnet.gatekeeper.GateKeeper;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public final class CommandRegister extends ListenerAdapter {
    private final GateKeeper gateKeeper;

    public CommandRegister(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        final var guild = event.getJDA().getGuildById(848176216011046962L);

        gateKeeper.getLogger().info("Has Guild? " + (guild != null));

        if(guild == null) {
            return;
        }

        guild.updateCommands().addCommands(
                Commands.slash("mcverify", "Verifies your discord account with a 1-time code")
                        .addOption(OptionType.STRING, "code", "The code given to you by the MC server", true)
        ).queue();
    }
}
