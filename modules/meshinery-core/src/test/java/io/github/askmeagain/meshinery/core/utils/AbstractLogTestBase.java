package io.github.askmeagain.meshinery.core.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public abstract class AbstractLogTestBase {

  protected void assertThatLogContainsMessage(CapturedOutput output, String... regexes) {
    var logs = output.getAll();
    assertThat(logs).isNotEmpty();
    assertThat(regexes)
        .allSatisfy(regex -> {
          assertThat(logs).contains(regex);
        });
  }
}
