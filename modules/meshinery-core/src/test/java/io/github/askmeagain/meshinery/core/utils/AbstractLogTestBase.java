package io.github.askmeagain.meshinery.core.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractLogTestBase {

  protected ListAppender<ILoggingEvent> listAppender;

  abstract protected Class<?> loggerToUse();

  @BeforeEach
    //https://stackoverflow.com/a/52229629/5563263
  void setup() {
    // create and start a ListAppender
    Logger fooLogger = (Logger) LoggerFactory.getLogger(loggerToUse());

    listAppender = new ListAppender<>();
    listAppender.start();

    // add the appender to the logger
    fooLogger.addAppender(listAppender);
  }

  protected void assertThatLogContainsMessage(String... regexes) {
    assertThat(regexes)
        .allSatisfy(regex -> {
          assertThat(getLogs())
              .extracting(ILoggingEvent::getFormattedMessage)
              .anySatisfy(log -> {
                assertThat(log.contains(regex)).isTrue();
              });
        });
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
