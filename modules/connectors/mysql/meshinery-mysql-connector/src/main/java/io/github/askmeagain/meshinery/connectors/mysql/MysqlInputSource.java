package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.other.Blocking;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class MysqlInputSource<C extends DataContext> implements AccessingInputSource<String, C> {

  public static final String SELECT_QUERY = """
      SELECT eid,context
      FROM <TABLE>
      WHERE processed = 0 AND state IN (<STATES>)
      ORDER BY eid
      LIMIT :limit
      """;

  private static final String SPECIFIC_SELECT_QUERY = """
      SELECT eid,context
      FROM <TABLE>
      WHERE processed = 0 and id = :id and state = :state
      LIMIT 1
      """;

  @Getter
  private final String name;
  private final Jdbi jdbi;
  private final Class<C> clazz;
  private final MeshineryMysqlProperties mysqlProperties;

  @Override
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public Optional<C> getContext(String key, String id) {

    return jdbi.inTransaction(handle -> {

      var qualifiedType = QualifiedType.of(clazz).with(Json.class);

      var firstResult = Blocking.byKey(
          key,
          () -> handle.createQuery(SPECIFIC_SELECT_QUERY)
              .bind("state", key)
              .define("TABLE", clazz.getSimpleName())
              .bind("id", id)
              .mapTo(qualifiedType)
              .findFirst()
      );

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
  public List<C> getInputs(List<String> keys) {
    var simpleKey = String.join("-", keys);
    return jdbi.inTransaction(handle -> {

      var qualifiedType = QualifiedType.of(clazz).with(Json.class);

      //TODO
      return Blocking.byKey(
          simpleKey,
          () -> {
            var result = handle.createQuery(SELECT_QUERY)
                .define("TABLE", clazz.getSimpleName())
                .bindList("STATES", keys)
                .bind("limit", mysqlProperties.getLimit())
                .mapTo(qualifiedType)
                .list();

            if (result.isEmpty()) {
              return Collections.emptyList();
            }

            var preparedIds = result.stream()
                .map(DataContext::getId)
                .collect(Collectors.toList());

            handle.createUpdate("UPDATE <TABLE> SET processed = 1 WHERE context -> '$.id' IN (<LIST>)")
                .bindList("LIST", preparedIds)
                .define("TABLE", clazz.getSimpleName())
                .execute();

            return result;
          }
      );
    });
  }
}

