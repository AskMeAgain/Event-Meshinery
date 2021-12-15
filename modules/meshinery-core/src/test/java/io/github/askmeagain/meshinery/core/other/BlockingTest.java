package io.github.askmeagain.meshinery.core.other;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class BlockingTest {

  public static final int MILLIS = 500;

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({"abc,2", "abcd,1"})
  void blockByKey(String secondKey, int expected) {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var flag = new AtomicInteger();
    var flag2 = new AtomicInteger();

    //Act ------------------------------------------------------------------------------------
    executor.execute(() -> Blocking.byKey("abc", () -> {
      update(flag);
      return null;
    }));
    Blocking.byKey("def", () -> {
      flag2.set(1);
      return null;
    });

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag.get()).isEqualTo(0);
    assertThat(flag2.get()).isEqualTo(1);

    //Act ------------------------------------------------------------------------------------
    Blocking.byKey(secondKey, () -> {
      flag.set(2);
      return null;
    });
    Thread.sleep(MILLIS + 100);

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag.get()).isEqualTo(expected);
  }

  @SneakyThrows
  private void update(AtomicInteger counter) {
    Thread.sleep(MILLIS);
    counter.set(1);
  }
}