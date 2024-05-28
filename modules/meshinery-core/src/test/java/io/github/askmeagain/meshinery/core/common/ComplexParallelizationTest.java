package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;

class ComplexParallelizationTest extends AbstractTestBase {

  //TODO fix this
  //  @Test
  //  @SuppressWarnings("unchecked")
  //  void testComplexParallelization() throws InterruptedException {
  //    //Arrange ---------------------------------------------------------------------------------
  //    var executor = Executors.newFixedThreadPool(3);
  //    var inputSource = TestInputSource.<TestContext>builder()
  //        .todo(new TestContext(0))
  //        .build();
  //
  //    MeshinerySourceConnector<String, TestContext> outputMock = Mockito.mock(MeshinerySourceConnector.class);
  //
  //    var task = MeshineryTaskFactory.<String, TestContext>builder()
  //        .read(executor, "Test")
  //        .inputSource(inputSource)
  //        .outputSource(outputMock)
  //        .process(ParallelProcessor.<TestContext>builder()
  //            .parallel(FluidProcessor.<TestContext>builder()
  //                .process(new ToTestContext2Processor(1))
  //                .process(new ToTestContextProcessor(2)))
  //            .parallel(new TestContextProcessor(30))
  //            .parallel(new TestContextProcessor(30))
  //            .parallel(new TestContextProcessor(30))
  //            .combine(this::getCombine))
  //        .write("")
  //        .build();
  //
  //    //Act -------------------------------------------------------------------------------------
  //    RoundRobinScheduler.<String, TestContext>builder()
  //        .isBatchJob(true)
  //        .task(task)
  //        .gracePeriodMilliseconds(0)
  //        .buildAndStart();
  //    var batchJobFinished = executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
  //
  //    //Assert ----------------------------------------------------------------------------------
  //    var argumentCapture = ArgumentCaptor.forClass(TestContext.class);
  //    Mockito.verify(outputMock).writeOutput(eq(""), argumentCapture.capture(), any());
  //    assertThat(batchJobFinished).isTrue();
  //    assertThat(argumentCapture.getValue())
  //        .extracting(TestContext::getIndex)
  //        .isEqualTo(93);
  //  }
}
