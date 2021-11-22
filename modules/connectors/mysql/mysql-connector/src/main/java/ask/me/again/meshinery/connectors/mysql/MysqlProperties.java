package ask.me.again.meshinery.connectors.mysql;

import lombok.Data;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MysqlProperties {

  public static String MYSQL_OVERRIDE_EXISTING = "mysql.override-existing";

  int limit = 10;
  String user;
  String password;
  String connectionString;
}
