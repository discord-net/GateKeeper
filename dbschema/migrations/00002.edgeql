CREATE MIGRATION m1nv3qgmctomgf7gb6kgwglyd4wwree4nkyiw442qwvr4ukuqk3lpa
    ONTO m1gbjcextns54tq4w4dizaarwuaoeg2gqgr6dr5pl3ogzzjdlmuora
{
  ALTER TYPE default::Player {
      ALTER PROPERTY discord_id {
          CREATE CONSTRAINT std::exclusive;
      };
      ALTER PROPERTY minecraft_id {
          CREATE CONSTRAINT std::exclusive;
      };
  };
  ALTER TYPE default::VerifyCode {
      ALTER LINK player {
          CREATE CONSTRAINT std::exclusive;
      };
  };
};
