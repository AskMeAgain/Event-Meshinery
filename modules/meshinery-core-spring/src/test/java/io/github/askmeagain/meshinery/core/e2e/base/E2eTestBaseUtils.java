package io.github.askmeagain.meshinery.core.e2e.base;

import java.util.List;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.ITEMS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_0;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_1;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_2;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_3;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@UtilityClass
public class E2eTestBaseUtils {

  public void setupTest() {
    RESULT_MAP_0.clear();
    RESULT_MAP_1.clear();
    RESULT_MAP_2.clear();
    RESULT_MAP_3.clear();
  }

  public void assertResultMap() {
    var resultSet = getResultSet();
    assertThat(RESULT_MAP_0).containsKeys(resultSet.toArray(String[]::new));
    assertThat(RESULT_MAP_1).containsKeys(resultSet.toArray(String[]::new));
    assertThat(RESULT_MAP_2).containsKeys(resultSet.toArray(String[]::new));
    assertThat(RESULT_MAP_3).containsKeys(resultSet.toArray(String[]::new));
  }

  private List<String> getResultSet() {
    return IntStream.range(1, ITEMS + 1).boxed().map(Object::toString).toList();
  }
}

