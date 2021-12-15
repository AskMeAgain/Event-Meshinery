package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MysqlOutputSource<C extends DataContext> implements OutputSource<String, C> {

  private static final String INSERT = """
      INSERT INTO <TABLE> (id,context,state,processed)
      VALUES (:ID, :CONTEXT, :STATE, 0)
      """;

  private static final String OVERRIDE = """
      INSERT INTO <TABLE> (id,context,state,processed)
      VALUES (:ID, :CONTEXT, :STATE, 0)
      ON DUPLICATE KEY UPDATE
        context = :CONTEXT,
        processed = 0
      """;

  @Getter
  private final String name;
  private final Jdbi jdbi;
  private final Class<C> clazz;

  @Override
  @SneakyThrows
  public void writeOutput(String key, C output) {

    var insertOverride = getTaskData().has(MeshineryMysqlProperties.MYSQL_OVERRIDE_EXISTING);
    var insertStatement = insertOverride ? OVERRIDE : INSERT;

    var qualifiedType = QualifiedType.of(clazz).with(Json.class);
    jdbi.useHandle(h -> h.createUpdate(insertStatement)
        .define("TABLE", clazz.getSimpleName())
        .bindByType("CONTEXT", output, qualifiedType)
        .bind("STATE", key)
        .bind("ID", output.getId())
        .execute()
    );
  }
}

