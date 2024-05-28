package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;

class SimpleParallelizationTest extends AbstractTestBase {

  public static final String KEY = "Test";

  //TODO fix this
  //  @RepeatedTest(10)
  //  @SuppressWarnings("unchecked")
  //  void testSimpleParallelization() throws InterruptedException {
  //    //Arrange ---------------------------------------------------------------------------------
  //    var executor = Executors.newFixedThreadPool(3);
  //
  //    MeshinerySourceConnector<String, TestContext> outputSource = Mockito.mock(MeshinerySourceConnector.class);
  //
  //    var inputSource = TestInputSource.<TestContext>builder()
  //        .todo(new TestContext(0))
  //        .build();
  //
  //    var task = MeshineryTaskFactory.<String, TestContext>builder()
  //        .read(executor, KEY)
  //        .inputSource(inputSource)
  //        .outputSource(outputSource)
  //        .process(ParallelProcessor.<TestContext>builder()
  //            .parallel(new TestContextProcessor(3))
  //            .parallel(new TestContextProcessor(3))
  //            .combine(this::getCombine))
  //        .write(KEY)
  //        .build();
  //
  //    //Act -------------------------------------------------------------------------------------
  //    RoundRobinScheduler.<String, TestContext>builder()
  //        .isBatchJob(true)
  //        .task(task)
  //        .gracePeriodMilliseconds(0)
  //        .buildAndStart();
  //    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);
  //
  //    //Assert ----------------------------------------------------------------------------------
  //    assertThat(batchJobFinished).isTrue();
  //    Mockito.verify(outputSource).writeOutput(eq(KEY), any(), any());
  //
  //  }
}
