package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MysqlInputSource<C extends Context> implements InputSource<String, C> {

  public static final String SELECT_QUERY = "SELECT context FROM <TABLE> WHERE processed = 0 AND state = :state ORDER BY eid LIMIT :limit";
  private final Jdbi jdbi;
  private final Class<C> clazz;

  private final int limit = 1;

  @Override
  public List<C> getInputs(String key) {

    return jdbi.inTransaction(handle -> {

      var qualifiedType = QualifiedType.of(clazz).with(Json.class);

      var firstResult = handle.createQuery(SELECT_QUERY)
        .bind("state", key)
        .define("TABLE", clazz.getSimpleName())
        .bind("limit", limit)
        .mapTo(qualifiedType)
        .list();

      var preparedIds = firstResult.stream()
        .map(Context::getId)
        .collect(Collectors.toList());

      handle.createUpdate("UPDATE <TABLE> SET processed = 1 WHERE context -> '$.id' IN (<LIST>)")
        .bindList("LIST", preparedIds)
        .define("TABLE", clazz.getSimpleName())
        .execute();

      return firstResult;
    });
  }
}

