CREATE MIGRATION m1de64fyb7ycfc75qigq4ulg6rwguucykzmogyd2dko5asfunr4cba
    ONTO m14uyw5vofnitbxvgcvgpiycou56bw4iguhtva4pfntx6kx5us6l2q
{
  ALTER TYPE default::Player {
      ALTER PROPERTY verified {
          SET default := false;
      };
  };
  ALTER TYPE default::VerifyCode {
      CREATE PROPERTY created_at: std::datetime {
          SET default := (std::datetime_current());
      };
  };
};
