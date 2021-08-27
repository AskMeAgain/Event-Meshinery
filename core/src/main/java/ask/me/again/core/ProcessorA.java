package ask.me.again.core;

public class ProcessorA implements ReactiveProcessor<TestContext> {

  @Override
  public TestContext process(TestContext context) {
    System.out.println("Received: " + context.getTestvalue1());
    return context.toBuilder()
      .testvalue1(context.getTestvalue1() + 1)
      .build();
  }
}
