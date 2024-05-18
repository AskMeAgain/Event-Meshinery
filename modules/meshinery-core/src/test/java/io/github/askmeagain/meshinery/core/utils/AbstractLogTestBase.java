package io.github.askmeagain.meshinery.core.utils;

import io.github.askmeagain.meshinery.core.utils.sources.OutputCapture;
import io.github.askmeagain.meshinery.core.utils.sources.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public abstract class AbstractLogTestBase {

  protected void assertThatLogContainsMessage(OutputCapture output, String... regexes) {
    var logs = output.getAll();
    assertThat(logs).isNotEmpty();
    assertThat(regexes)
        .allSatisfy(regex -> {
          assertThat(logs).contains(regex);
        });
  }
}
