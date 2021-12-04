package io.github.askmeagain.meshinery.connectors.mysql;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MysqlProperties {

  public static String MYSQL_OVERRIDE_EXISTING = "mysql.override-existing";

  int limit = 10;
  @NotBlank
  String user;
  String password;
  @NotBlank
  String connectionString;
}
