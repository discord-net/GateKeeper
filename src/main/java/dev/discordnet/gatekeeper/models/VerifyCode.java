package dev.discordnet.gatekeeper.models;

import com.edgedb.driver.annotations.EdgeDBType;

import java.time.OffsetDateTime;
import java.util.UUID;

@EdgeDBType
public final class VerifyCode {
    public UUID id;
    public Player player;
    public String code;
    public OffsetDateTime created_at;
}
