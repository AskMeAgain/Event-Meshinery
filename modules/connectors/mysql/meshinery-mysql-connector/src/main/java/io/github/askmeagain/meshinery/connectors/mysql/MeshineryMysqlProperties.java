package io.github.askmeagain.meshinery.connectors.mysql;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryMysqlProperties {

  public static String MYSQL_OVERRIDE_EXISTING = "mysql.override-existing";

  @Positive
  int limit = 10;
  @NotBlank
  String user;
  String password;
  @NotBlank
  String connectionString;
  @NotBlank
  String schema;
}
