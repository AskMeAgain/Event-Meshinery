package io.github.askmeagain.meshinery.core.utils;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class LogTestBase {

  protected ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
    //https://stackoverflow.com/a/52229629/5563263
  void setup() {
    // get Logback Logger
    Logger fooLogger = (Logger) LoggerFactory.getLogger(RoundRobinScheduler.class);

    // create and start a ListAppender
    listAppender = new ListAppender<>();
    listAppender.start();

    // add the appender to the logger
    fooLogger.addAppender(listAppender);
  }

  protected void assertThatLogContainsMessage(String message) {
    assertThat(getLogs())
        .extracting(ILoggingEvent::getFormattedMessage)
        .contains(message);
  }

  protected void assertThatNoErrorThrown() {
    assertThat(getLogs())
        .extracting(ILoggingEvent::getLevel)
        .doesNotContain(Level.ERROR);
  }

  private List<ILoggingEvent> getLogs() {
    return listAppender.list;
  }

  @AfterEach
  void teardown() {
    listAppender.clearAllFilters();
  }
}
