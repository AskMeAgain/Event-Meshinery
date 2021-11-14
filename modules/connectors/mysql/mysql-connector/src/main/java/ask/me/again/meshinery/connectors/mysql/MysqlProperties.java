package ask.me.again.meshinery.connectors.mysql;

import lombok.Data;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MysqlProperties {
  int limit;
  String user;
  String password;
  String connectionString;
}
