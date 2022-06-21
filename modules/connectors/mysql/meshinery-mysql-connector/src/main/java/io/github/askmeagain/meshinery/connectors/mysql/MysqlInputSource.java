package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.other.Blocking;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MysqlInputSource<C extends DataContext> implements AccessingInputSource<String, C> {

  private static final String SELECT_QUERY = """
      SELECT eid,context
      FROM <TABLE>
      WHERE processed = 0 AND state IN (<STATES>)
      ORDER BY eid ASC
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
  private final ObjectMapper objectMapper;
  private final Jdbi jdbi;
  private final Class<C> clazz;
  private final MeshineryMysqlProperties mysqlProperties;
  private final String simpleName;

  public MysqlInputSource(
      String name,
      ObjectMapper objectMapper,
      Jdbi jdbi,
      Class<C> clazz,
      MeshineryMysqlProperties mysqlProperties
  ) {
    this.name = name;
    this.objectMapper = objectMapper;
    this.jdbi = jdbi;
    this.clazz = clazz;
    this.mysqlProperties = mysqlProperties;
    this.simpleName = clazz.getSimpleName();
  }

  @Override
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public Optional<C> getContext(String key, String id) {
    return jdbi.inTransaction(handle -> Blocking.byKey(
        key,
        () -> {
          var firstResult = handle.createQuery(SPECIFIC_SELECT_QUERY)
              .bind("state", key)
              .define("TABLE", clazz.getSimpleName())
              .bind("id", id)
              .mapToBean(InternalWrapper.class)
              .findFirst();

          if (firstResult.isEmpty()) {
            return Optional.empty();
          }

          handle.createUpdate("UPDATE <TABLE> SET processed = 1 WHERE eid = :eid")
              .define("TABLE", clazz.getSimpleName())
              .bind("eid", firstResult.get().getEid())
              .execute();

          return firstResult.map(x -> {
            try {
              return objectMapper.readValue(x.getContext(), clazz);
            } catch (JsonProcessingException e) {
              e.printStackTrace();
              return null;
            }
          });
        }
    ));

  }

  @Override
  @SneakyThrows
  public List<C> getInputs(List<String> keys) {
    return jdbi.withHandle(handle -> Blocking.byKey(
        keys.toArray(String[]::new),
        () -> {
          var result = handle.createQuery(SELECT_QUERY)
              .define("TABLE", clazz.getSimpleName())
              .bindList("STATES", keys)
              .bind("limit", mysqlProperties.getLimit())
              .mapToBean(InternalWrapper.class)
              .list();

          if (result.isEmpty()) {
            return Collections.emptyList();
          }

          var preparedIds = result.stream()
              .map(InternalWrapper::getEid)
              .toList();

          var finalResult = result.stream()
              .map(InternalWrapper::getContext)
              .map(x -> {
                try {
                  return objectMapper.readValue(x, clazz);
                } catch (JsonProcessingException e) {
                  e.printStackTrace();
                  return null;
                }
              })
              .filter(Objects::nonNull)
              .toList();

          handle.createUpdate("UPDATE <TABLE> SET processed = 1 WHERE eid IN (<LIST>)")
              .bindList("LIST", preparedIds)
              .define("TABLE", clazz.getSimpleName())
              .execute();

          return finalResult;
        }
    ));
  }
}

