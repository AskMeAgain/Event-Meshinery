package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class MysqlInputSource<C extends Context> implements InputSource<String, C> {


  public static final String SELECT_QUERY = """
      SELECT context 
      FROM <TABLE> 
      WHERE processed = 0 AND state = :state 
      ORDER BY eid 
      LIMIT :limit
      """;

  private static final String SPECIFIC_SELECT_QUERY = """
      SELECT context
      FROM <TABLE>
      WHERE processed = 0 and state = :state and id = :id
      LIMIT 1
      """;

  @Getter
  private final String sourceName;
  private final Jdbi jdbi;
  private final Class<C> clazz;

  private final MysqlProperties mysqlProperties;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public Optional<C> getContext(String key, String id) {

    return jdbi.inTransaction(handle -> {

      var qualifiedType = QualifiedType.of(clazz).with(Json.class);

      var firstResult = handle.createQuery(SPECIFIC_SELECT_QUERY)
          .bind("state", key)
          .define("TABLE", clazz.getSimpleName())
          .bind("id", id)
          .mapTo(qualifiedType)
          .findFirst();

      if (firstResult.isEmpty()) {
        return Optional.empty();
      }

      handle.createUpdate("UPDATE <TABLE> SET processed = 1 WHERE context -> '$.id' = :id")
          .define("TABLE", clazz.getSimpleName())
          .bind("id", firstResult.get().getId())
          .execute();

      return firstResult;
    });

  }

  @Override
  public List<C> getInputs(String key) {

    return jdbi.inTransaction(handle -> {

      var qualifiedType = QualifiedType.of(clazz).with(Json.class);

      var firstResult = handle.createQuery(SELECT_QUERY)
          .bind("state", key)
          .define("TABLE", clazz.getSimpleName())
          .bind("limit", mysqlProperties.getLimit())
          .mapTo(qualifiedType)
          .list();

      if (firstResult.isEmpty()) {
        return Collections.emptyList();
      }

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

