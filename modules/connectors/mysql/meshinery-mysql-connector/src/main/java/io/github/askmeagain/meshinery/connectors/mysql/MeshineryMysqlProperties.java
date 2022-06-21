package io.github.askmeagain.meshinery.connectors.mysql;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
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
}
