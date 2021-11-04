package ask.me.again.meshinery.core.source.cron;

import ask.me.again.meshinery.core.common.context.TestContext;
import com.cronutils.model.CronType;
import java.time.Instant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CronInputSourceTest {

  @SneakyThrows
  @RepeatedTest(100)
  void testCronInputSecond() {
    //Arrange ----------------------------------------------------------------------------------------------------------1
    long currentSecond = getCurrentSecond();

    var cron = currentSecond + "/2 * * * * *";
    var source = new CronInputSource<>(CronType.SPRING, () -> new TestContext(0));

    //Act --------------------------------------------------------------------------------------------------------------
    var resultEmpty = source.getInputs(cron);
    Thread.sleep(900);
    var resultEmpty2 = source.getInputs(cron);
    Thread.sleep(1100);
    var result = source.getInputs(cron);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(resultEmpty).isEmpty();
    assertThat(resultEmpty2).isEmpty();
    assertThat(result).hasSize(1);

  }

  private long getCurrentSecond() throws InterruptedException {
    Instant now = Instant.now();
    long epochSecond = now.getEpochSecond();
    System.out.println(now);
    var currentSecond = epochSecond % 60;
    if (currentSecond > 57) {
      //this is a bit hacky, but the tests fail around 58 so we just wait 4 seconds and try again
      Thread.sleep(4000);
      return getCurrentSecond();
    }
    return currentSecond;
  }
}