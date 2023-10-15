package dev.discordnet.gatekeeper.discord;

import dev.discordnet.gatekeeper.GateKeeper;
import dev.discordnet.gatekeeper.models.VerifyCode;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MCVerifyCommand extends ListenerAdapter {
    private final GateKeeper gateKeeper;

    public MCVerifyCommand(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    @Override
    public void onSlashCommandInteraction(final @NotNull SlashCommandInteractionEvent event) {
        if(!event.getName().equals("mcverify")) {
            return;
        }

        final var code = event.getOption("code");

        if(code == null) {
            gateKeeper.getLogger().warning("No code provided in MCVerify command");
            event.reply("The parameter `code` is required!").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        gateKeeper.getEdgeDB().querySingle(
                VerifyCode.class,
                "SELECT VerifyCode { id, created_at, player: { id } } FILTER .code = <str>$code",
                new HashMap<>(){{
                    put("code", code.getAsString());
                }}
        ).thenCompose(verifyCode -> {
            gateKeeper.getLogger().info("Checking user verification code");

            if(verifyCode == null) {
                gateKeeper.getLogger().info("Code was invalid");
                event.getHook().editOriginal("That code is invalid!").queue();
                return CompletableFuture.completedFuture(null);
            }

            if(verifyCode.created_at.plus(Duration.of(5, ChronoUnit.MINUTES)).isBefore(OffsetDateTime.now())) {
                gateKeeper.getLogger().info("Code was expired");
                // expired
                event.getHook().editOriginal("The code you have provided is expired, please rejoin the server to get a new one").queue();
                return CompletableFuture.completedFuture(null);
            }

            gateKeeper.getLogger().info("Code was valid, whitelisting " + verifyCode.player.discordId + " : " + verifyCode.player.minecraftId);

            return gateKeeper.getEdgeDB().execute(
                    "WITH " +
                    "d := (DELETE VerifyCode FILTER .id = <uuid>$cid) " +
                    "UPDATE Player FILTER .id = <uuid>$pid SET { verified := true, discord_id := <str>$did }",
                    new HashMap<>(){{
                        put("cid", verifyCode.id);
                        put("pid", verifyCode.player.id);
                        put("did", event.getUser().getId());
                    }}
            ).thenAccept(v -> {
                gateKeeper.getLogger().info("Verification complete");
                event.getHook().editOriginal("Congrats! You've been verified, you can now login and play!").queue();
            });
        }).exceptionally(e -> {
            gateKeeper.getLogger().log(Level.SEVERE, e, () -> "Failed to run code check");
            event.getHook().editOriginal("Failed to verify your code, this is a bug with the system itself :(").queue();
            return null;
        });
    }
}
