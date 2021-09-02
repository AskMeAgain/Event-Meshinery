package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MysqlInputSource<C extends Context> implements InputSource<String, C> {

  public static final String SELECT_CONTEXT_FROM_TABLE_WHERE_PROCESSED_0 = """
    SELECT context FROM <TABLE> WHERE processed = 0
    """;
  private final Jdbi jdbi;
  private final Class<C> clazz;

  @Override
  public List<C> getInputs(String key) {

    return jdbi.inTransaction(handle -> {
      var firstResult = handle.createQuery(SELECT_CONTEXT_FROM_TABLE_WHERE_PROCESSED_0)
        .bind("TABLE", clazz.getSimpleName())
        .mapToBean(clazz)
        .list();
      var preparedIds = firstResult.stream()
        .map(Context::getId)
        .collect(Collectors.toList());

      handle.createUpdate("SET processed = 1 WHERE eid IN (<LIST>)")
        .bindList("LIST", preparedIds)
        .execute();
      return firstResult;
    });
  }

  @Value
  @Builder
  public static class StateContainer<C> {
    C context;
  }
}

