package io.github.askmeagain.meshinery.core.e2e.base;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestConfiguration.ITEMS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestConfiguration.NUMBER_OF_TOPICS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestConfiguration.RESULT_MAP;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@UtilityClass
public class E2eTestBaseUtils {

  public void setupTest() {
    RESULT_MAP.clear();
    IntStream.range(1, NUMBER_OF_TOPICS + 1)
        .forEach(i -> RESULT_MAP.put(i, new ArrayList<>()));
  }

  public void assertResultMap() {
    assertThat(RESULT_MAP)
        .extracting(Map::values)
        .satisfies(x -> assertThat(x)
            .allSatisfy(y -> assertThat(y).containsExactlyInAnyOrder(getResultSet())));
  }

  private String[] getResultSet() {
    return IntStream.range(1, ITEMS + 1)
        .mapToObj(i -> "" + i)
        .toArray(String[]::new);
  }
}

