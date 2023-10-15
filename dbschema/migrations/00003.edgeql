CREATE MIGRATION m14uyw5vofnitbxvgcvgpiycou56bw4iguhtva4pfntx6kx5us6l2q
    ONTO m1nv3qgmctomgf7gb6kgwglyd4wwree4nkyiw442qwvr4ukuqk3lpa
{
  ALTER TYPE default::Player {
      ALTER PROPERTY minecraft_id {
          SET REQUIRED USING (<std::uuid>{});
      };
  };
};
