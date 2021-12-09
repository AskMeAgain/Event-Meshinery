package io.github.askmeagain.meshinery.core.other;

import java.util.HashSet;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataInjectingExecutorServiceTest {

  @Test
  void testHashSetCollection() {
    //Arrange --------------------------------------------------------------------------------
    var executor12 = Executors.newSingleThreadExecutor();
    var executor3 = Executors.newFixedThreadPool(2);

    var wrappedExecutor1 = new DataInjectingExecutorService("asd", executor12);
    var wrappedExecutor2 = new DataInjectingExecutorService("asd", executor12);
    var wrappedExecutor3 = new DataInjectingExecutorService("asd", executor3);
    var set = new HashSet<DataInjectingExecutorService>();

    //Act ------------------------------------------------------------------------------------
    set.add(wrappedExecutor1);
    set.add(wrappedExecutor2);
    set.add(wrappedExecutor3);

    //Assert ---------------------------------------------------------------------------------
    assertThat(set).hasSize(2)
        .contains(wrappedExecutor2, wrappedExecutor3);
  }

}