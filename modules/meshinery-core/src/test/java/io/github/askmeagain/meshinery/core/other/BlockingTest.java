package io.github.askmeagain.meshinery.core.other;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class BlockingTest {

  private static final int MILLIS = 300;
  private static final String KEY = "abasdasdasdasdc";

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({KEY + ",2", KEY + "d,1"})
  void blockByKey(String secondKey, int expected) {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(3);
    var flag = new AtomicInteger();
    var flag2 = new AtomicInteger();

    //Act ------------------------------------------------------------------------------------
    executor.execute(() -> Blocking.byKey(KEY, () -> {
      update(flag);
      return null;
    }));
    Blocking.byKey(KEY + "doesnt matter this key here", () -> {
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
    Thread.sleep(MILLIS + 2000);

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag.get()).isEqualTo(expected);
    executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
  }

  @SneakyThrows
  private void update(AtomicInteger counter) {
    Thread.sleep(MILLIS);
    counter.set(1);
  }
}