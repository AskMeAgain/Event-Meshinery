package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.OutputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
@RequiredArgsConstructor
public class MysqlOutputSource<C extends Context> implements OutputSource<String, C> {

  private final Jdbi jdbi;
  private final Class<C> clazz;
  private final ObjectMapper objectMapper;

  @SneakyThrows
  @Override
  public void writeOutput(String key, C output) {

    jdbi.useHandle(handle -> handle.createUpdate("INSERT INTO <TABLE> (context,state,processed) VALUES (:CONTEXT, :STATE, :PROCESSED)")
      .define("TABLE", clazz.getSimpleName())
      .bind("CONTEXT", objectMapper.writeValueAsString(output))
      .bind("STATE", key)
      .bind("PROCESSED", 0)
      .execute()
    );
  }
}

