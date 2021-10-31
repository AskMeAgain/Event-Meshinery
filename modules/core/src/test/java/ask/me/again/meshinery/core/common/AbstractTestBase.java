package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.context.TestContext2;
import java.util.List;

public abstract class AbstractTestBase {

  protected TestContext2 map(TestContext context) {
    return new TestContext2(context.getIndex() + 1);
  }

  protected TestContext map(TestContext2 context) {
    return new TestContext(context.getIndex() + 1);
  }

  protected TestContext getCombine(List<TestContext> list) {
    var sum = list.stream().mapToInt(TestContext::getIndex).sum();
    return new TestContext(sum);
  }
}
