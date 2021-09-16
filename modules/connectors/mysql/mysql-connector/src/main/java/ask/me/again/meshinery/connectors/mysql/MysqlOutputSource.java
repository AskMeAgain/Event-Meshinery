package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.OutputSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

@Slf4j
@RequiredArgsConstructor
public class MysqlOutputSource<C extends Context> implements OutputSource<String, C> {

  private final Jdbi jdbi;
  private final Class<C> clazz;

  @SneakyThrows
  @Override
  public void writeOutput(String key, C output) {

    var qualifiedType = QualifiedType.of(clazz).with(Json.class);

    jdbi.useHandle(handle -> handle.createUpdate("INSERT INTO <TABLE> (context,state,processed) VALUES (:CONTEXT, :STATE, 0)")
      .define("TABLE", clazz.getSimpleName())
      .bindByType("CONTEXT", output, qualifiedType)
      .bind("STATE", key)
      .execute()
    );
  }
}
