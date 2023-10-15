package dev.discordnet.gatekeeper.events;

import dev.discordnet.gatekeeper.GateKeeper;
import dev.discordnet.gatekeeper.models.VerifyCode;
import dev.discordnet.gatekeeper.models.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class PlayerJoined implements Listener {
    private final GateKeeper gateKeeper;
    private final Random random;

    public PlayerJoined(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoined(AsyncPlayerPreLoginEvent ev) throws ExecutionException, InterruptedException {
        if(
                gateKeeper.getServer().getWhitelistedPlayers().contains(Bukkit.getOfflinePlayer(ev.getUniqueId())) ||
                gateKeeper.getServer().getOperators().contains(Bukkit.getOfflinePlayer(ev.getUniqueId())))
        {
            ev.allow();
            gateKeeper.getLogger().log(Level.INFO, ev.getUniqueId() + " is whitelisted, bypassing");
            return;
        }

        gateKeeper.getEdgeDB().queryRequiredSingle(
                Player.class,
                "WITH " +
                "player := (INSERT Player { minecraft_id := <uuid>$mcid } UNLESS CONFLICT ON .minecraft_id ELSE (SELECT Player)) " +
                "SELECT player { id, minecraft_id, verified, verifyCode := .<player[IS VerifyCode] { code, created_at}}",
                 new HashMap<>(){{
                     put("mcid", ev.getUniqueId());
                 }}
        ).thenCompose(player -> {
            if(player.verified) {
                ev.allow();
                return CompletableFuture.completedFuture(null);
            }

            if(player.verifyCode != null && !isExpired(player.verifyCode)) {
                ev.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getVerifyMessage(player.verifyCode));
                return CompletableFuture.completedFuture(null);
            }

            return generateUniqueCode(player)
                    .thenAccept(code -> {
                        ev.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getVerifyMessage(code));
                    });
        }).exceptionally(e -> {
            ev.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "The code behind this shitty plugin has failed, contact quinchs");
            gateKeeper.getLogger().log(Level.SEVERE, e, () -> "Failed to check for whitelist on " + ev.getUniqueId());
            return null;
        }).toCompletableFuture().get();

        gateKeeper.getLogger().info("Player result: " + ev.getUniqueId() + " : " + ev.getLoginResult());
    }

    private final CompletionStage<VerifyCode> generateUniqueCode(Player player) {
        final var code = generateString();
        return gateKeeper.getEdgeDB()
                .queryRequiredSingle(
                        VerifyCode.class,
                        "WITH " +
                                "p := (SELECT Player FILTER .id = <uuid>$pid), " +
                                "c := (INSERT VerifyCode { player := p, code := <str>$code } UNLESS CONFLICT ON .player ELSE (SELECT VerifyCode))" +
                                "SELECT c { code, created_at }",
                        new HashMap<>(){{
                            put("pid", player.id);
                            put("code", code);
                        }}
                );
    }

    private final String getVerifyMessage(VerifyCode code) {
        return "In order to play you must run the \u00a7l/mcverify\u00a7r slash command in the \u00a7lDiscord.Net\u00a7r guild with the following code: \u00a7a\u00a7l" +
                code.code +
                "\u00a7r\nThe code will reset at \u00a7c\u00a7l" +
                code.created_at.plus(Duration.of(5, ChronoUnit.MINUTES)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private final boolean isExpired(VerifyCode code) {
        return code.created_at.plus(Duration.of(5, ChronoUnit.MINUTES)).isBefore(OffsetDateTime.now());
    }

    private final String generateString() {
        final var keyset = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder sb = new StringBuilder(7);
        for(int i = 0; i < 7; i++)
            sb.append(keyset.charAt(random.nextInt(keyset.length())));
        return sb.toString();
    }
}
