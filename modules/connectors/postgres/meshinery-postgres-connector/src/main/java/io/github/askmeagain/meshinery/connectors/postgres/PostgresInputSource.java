package io.github.askmeagain.meshinery.connectors.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.other.Blocking;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class PostgresInputSource<C extends DataContext> implements AccessingInputSource<String, C> {

  private static final String SELECT_QUERY = """
      UPDATE <TABLE>
      SET processed = true
      WHERE eid IN
      (
        SELECT eid
        FROM <TABLE>
        WHERE processed = false AND state IN (<STATES>)
        ORDER BY eid ASC
        FOR UPDATE SKIP LOCKED
        LIMIT :limit
      )
      RETURNING *;
      """;

  private static final String SPECIFIC_SELECT_QUERY = """
      UPDATE <TABLE>
      SET processed = true
      WHERE eid IN
      (
        SELECT eid
        FROM <TABLE>
        WHERE processed = false and id = :id and state = :state
        ORDER BY eid ASC
        FOR UPDATE SKIP LOCKED
        LIMIT 1
      )
      RETURNING *;
      """;

  @Getter
  private final String name;
  private final ObjectMapper objectMapper;
  private final Jdbi jdbi;
  private final Class<C> clazz;
  private final MeshineryPostgresProperties postgresProperties;
  private final String simpleName;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PostgresInputSource(
      String name,
      ObjectMapper objectMapper,
      Jdbi jdbi,
      Class<C> clazz,
      MeshineryPostgresProperties postgresProperties
  ) {
    this.name = name;
    this.objectMapper = objectMapper;
    this.jdbi = jdbi;
    this.clazz = clazz;
    this.postgresProperties = postgresProperties;
    this.simpleName = clazz.getSimpleName();
  }

  @Override
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public Optional<C> getContext(String key, String id) {
    return jdbi.inTransaction(handle -> Blocking.byKey(
        key,
        () -> handle.createQuery(SPECIFIC_SELECT_QUERY)
            .bind("state", key)
            .define("TABLE", simpleName)
            .bind("id", id)
            .mapToBean(InternalWrapper.class)
            .findFirst()
            .map(x -> {
              try {
                return objectMapper.readValue(x.getContext(), clazz);
              } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
              }
            })
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
              .bind("limit", postgresProperties.getLimit())
              .mapToBean(InternalWrapper.class)
              .list();

          return result.stream()
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
        }
    ));
  }
}

