package io.github.askmeagain.meshinery.core.utils;

import org.junit.jupiter.api.extension.ExtendWith;
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
