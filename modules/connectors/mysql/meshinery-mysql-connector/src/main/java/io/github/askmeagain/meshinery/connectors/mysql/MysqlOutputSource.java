package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MysqlOutputSource<C extends DataContext> implements OutputSource<String, C> {

  private static final String INSERT = """
      INSERT INTO <TABLE> (id,context,state)
      VALUES (:ID, :CONTEXT, :STATE)
      """;

  private static final String OVERRIDE = """
      INSERT INTO <TABLE> (id,context,state)
      VALUES (:ID, :CONTEXT, :STATE)
      ON DUPLICATE KEY UPDATE
        context = :CONTEXT,
        processed = 0
      """;

  @Getter
  private final String name;
  private final Jdbi jdbi;
  private final String simpleName;
  private final QualifiedType<C> qualifiedType;

  public MysqlOutputSource(String name, Jdbi jdbi, Class<C> clazz) {
    this.name = name;
    this.jdbi = jdbi;
    qualifiedType = QualifiedType.of(clazz).with(Json.class);
    simpleName = clazz.getSimpleName();
  }

  @Override
  @SneakyThrows
  public void writeOutput(String key, C output) {
    var insertOverride = getTaskData().has(MeshineryMysqlProperties.MYSQL_OVERRIDE_EXISTING);
    var insertStatement = insertOverride ? OVERRIDE : INSERT;

    jdbi.useHandle(h -> h.createUpdate(insertStatement)
        .define("TABLE", simpleName)
        .bindByType("CONTEXT", output, qualifiedType)
        .bind("STATE", key)
        .bind("ID", output.getId())
        .execute()
    );
  }
}

