package dev.discordnet.gatekeeper.models;

import com.edgedb.driver.annotations.EdgeDBType;

import javax.annotation.Nullable;
import java.util.UUID;

@EdgeDBType
public final class Player {
    public UUID id;
    public UUID minecraftId;
    public String discordId;
    public boolean verified;

    public @Nullable VerifyCode verifyCode;
}
