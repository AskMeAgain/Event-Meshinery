package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.other.Blocking;
import java.util.Collection;
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
public class MysqlInputSource<C extends DataContext> implements AccessingInputSource<String, C> {

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
  private final String name;
  private final Jdbi jdbi;
  private final Class<C> clazz;
  private final MysqlProperties mysqlProperties;

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
    return keys.stream()
        .map(this::getInputs)
        .flatMap(Collection::stream)
        .toList();
  }

  private List<C> getInputs(String key) {

    return jdbi.inTransaction(handle -> {

      var qualifiedType = QualifiedType.of(clazz).with(Json.class);

      var firstResult = Blocking.byKey(
          key,
          () -> handle.createQuery(SELECT_QUERY)
              .bind("state", key)
              .define("TABLE", clazz.getSimpleName())
              .bind("limit", mysqlProperties.getLimit())
              .mapTo(qualifiedType)
              .list()
      );

      if (firstResult.isEmpty()) {
        return Collections.emptyList();
      }

      var preparedIds = firstResult.stream()
          .map(DataContext::getId)
          .collect(Collectors.toList());

      handle.createUpdate("UPDATE <TABLE> SET processed = 1 WHERE context -> '$.id' IN (<LIST>)")
          .bindList("LIST", preparedIds)
          .define("TABLE", clazz.getSimpleName())
          .execute();

      return firstResult;
    });
  }
}

