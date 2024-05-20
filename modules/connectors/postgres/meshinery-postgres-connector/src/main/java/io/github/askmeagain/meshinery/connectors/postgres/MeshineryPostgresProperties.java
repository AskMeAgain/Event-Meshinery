package io.github.askmeagain.meshinery.connectors.postgres;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryPostgresProperties {

  public static String POSTGRES_OVERRIDE_EXISTING = "postgres.override-existing";

  @Positive
  int limit = 10;
  @NotBlank
  String user;
  String password;
  @NotBlank
  String connectionString;
  @NotBlank
  String schema = "public";
}
