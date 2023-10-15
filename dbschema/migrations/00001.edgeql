CREATE MIGRATION m1gbjcextns54tq4w4dizaarwuaoeg2gqgr6dr5pl3ogzzjdlmuora
    ONTO initial
{
  CREATE TYPE default::Player {
      CREATE PROPERTY minecraft_id: std::uuid;
      CREATE INDEX ON (.minecraft_id);
      CREATE PROPERTY discord_id: std::str;
      CREATE PROPERTY verified: std::bool;
  };
  CREATE TYPE default::VerifyCode {
      CREATE LINK player: default::Player;
      CREATE PROPERTY code: std::str;
  };
};
