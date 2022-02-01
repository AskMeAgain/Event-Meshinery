package io.github.askmeagain.meshinery.core.other;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlockingTest {

  @SneakyThrows
  @Test
  void blockByKeyHappy1() {
    //Arrange --------------------------------------------------------------------------------
    var counter = new AtomicInteger();
    var executor = Executors.newSingleThreadExecutor();

    //Act ------------------------------------------------------------------------------------
    executor.execute(() -> Blocking.byKey("KEY", () -> waitAndApply(counter, 100, 10)));
    Blocking.byKey(new String[]{"KEY", "KEY2"}, () -> waitAndApply(counter, 0, 11));

    //Assert ---------------------------------------------------------------------------------
    assertThat(counter).hasValue(11);
  }

  @SneakyThrows
  @Test
  void blockByKeyHappy2() {
    //Arrange --------------------------------------------------------------------------------
    var counter = new AtomicInteger();
    var executor = Executors.newSingleThreadExecutor();

    //Act ------------------------------------------------------------------------------------
    executor.execute(() -> Blocking.byKey("KEY", () -> waitAndApply(counter, 100, 10)));
    Blocking.byKey(new String[]{"KEY3", "KEY2"}, () -> waitAndApply(counter, 0, 11));

    //Assert ---------------------------------------------------------------------------------
    assertThat(counter).hasValue(11);
  }

  @Test
  @SneakyThrows
  void blockByKeyHappy3() {
    //Arrange --------------------------------------------------------------------------------
    var counter = new AtomicInteger();
    var executor = Executors.newSingleThreadExecutor();

    //Act ------------------------------------------------------------------------------------
    executor.execute(() -> waitFirstThenBlock(counter));
    Blocking.byKey(new String[]{"KEY3", "KEY2"}, () -> waitAndApply(counter, 0, 11));

    //let the other thread write new value
    Thread.sleep(2000);
    //Assert ---------------------------------------------------------------------------------
    assertThat(counter).hasValue(1);
  }

  @Test
  @SneakyThrows
  void blockByKeyHappy4() {
    //Arrange --------------------------------------------------------------------------------
    var counter = new AtomicInteger();
    var executor = Executors.newSingleThreadExecutor();

    //Act ------------------------------------------------------------------------------------
    executor.execute(() -> waitFirstThenBlock(counter));
    Blocking.byKey(new String[]{"KEY", "KEY2"}, () -> waitAndApply(counter, 0, 11));

    //let the other thread write new value
    Thread.sleep(200);
    //Assert ---------------------------------------------------------------------------------
    assertThat(counter).hasValue(1);
  }

  @Test
  @SneakyThrows
  void blockingComplexTest() {
    //Arrange --------------------------------------------------------------------------------
    var counter = new AtomicInteger();
    var executor = Executors.newFixedThreadPool(11);

    //Act ------------------------------------------------------------------------------------
    executor.execute(() -> increment(counter, 0, "K", "ABC"));
    executor.execute(() -> increment(counter, 10, "K"));
    executor.execute(() -> increment(counter, 20, "K3", "K"));
    executor.execute(() -> increment(counter, 30, "K5", "K"));
    executor.execute(() -> check(counter, 0, 100, "Different1"));
    executor.execute(() -> check(counter, 1, 600, "Different2"));
    executor.execute(() -> check(counter, 2, 1100, "Different3"));
    executor.execute(() -> check(counter, 3, 1600, "Different4"));

    //let the other thread write new value
    Thread.sleep(3000);
    //Assert ---------------------------------------------------------------------------------
    assertThat(counter).hasValue(4);
  }

  @SneakyThrows
  private void increment(AtomicInteger counter, int startingTime, String... keys) {
    Thread.sleep(startingTime);
    Blocking.byKey(keys, () -> {
      waitSomeTime();
      System.out.println("Increment now!");
      counter.getAndIncrement();
      return "";
    });
  }

  @SneakyThrows
  private void check(AtomicInteger counter, int expected, int startingTime, String key) {
    Thread.sleep(startingTime);
    Blocking.byKey(key, () -> {
      var value = counter.get();
      if (value != expected) {
        System.out.println("Expected %s but got %s with starting time %s".formatted(expected, value, startingTime));
        counter.set(100);
      }
      return "";
    });
  }

  @SneakyThrows
  private void waitSomeTime() {
    Thread.sleep(500);
  }

  @SneakyThrows
  private String waitFirstThenBlock(AtomicInteger counter) {
    Thread.sleep(100);
    return Blocking.byKey("KEY", () -> waitAndApply(counter, 0, 1));
  }

  @NotNull
  @SneakyThrows
  private String waitAndApply(AtomicInteger counter, int millis, int newValue) {
    Thread.sleep(millis);
    counter.set(newValue);
    return "asd";
  }

}