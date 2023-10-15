module default {
  type Player {
    required minecraft_id: uuid {
      constraint exclusive;
    }

    discord_id: str {
      constraint exclusive;
    }

    verified: bool {
      default := false;
    }
    
    index on (.minecraft_id)
  }

  type VerifyCode {
    link player: Player {
      constraint exclusive;
    }
    code: str;
    created_at: datetime {
      default := datetime_current();
    }
  }
}
