package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MysqlInputSource<C extends Context> implements InputSource<String, C> {

  public static final String SELECT_QUERY = "SELECT context FROM <TABLE> WHERE processed = 0 AND state = :state ORDER BY eid LIMIT :limit";
  private final Jdbi jdbi;
  private final Class<C> clazz;
  private final ObjectMapper objectMapper;

  private final int limit = 1;

  @Override
  public List<C> getInputs(String key) {

    return jdbi.inTransaction(handle -> {
      var firstResult = handle.createQuery(SELECT_QUERY)
        .bind("state", key)
        .define("TABLE", clazz.getSimpleName())
        .bind("limit", limit)
        .mapTo(String.class)
        .list();

      var transformed = firstResult.stream()
        .map(x -> {
          try {
            return objectMapper.readValue(x, clazz);
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
          return null;
        }).collect(Collectors.toList());

      var preparedIds = transformed.stream()
        .map(Context::getId)
        .collect(Collectors.toList());

      handle.createUpdate("UPDATE <TABLE> SET processed = 1 WHERE context -> '$.id' IN (<LIST>)")
        .bindList("LIST", preparedIds)
        .define("TABLE", clazz.getSimpleName())
        .execute();

      return transformed;
    });
  }

  @Value
  @Builder
  public static class StateContainer<C> {
    @ColumnName("state")
    String state;
    @ColumnName("eid")
    long eid;
    @ColumnName("processed")
    boolean processed;
    @ColumnName("context")
    C context;
  }
}

