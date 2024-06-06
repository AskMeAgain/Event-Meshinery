package io.github.askmeagain.meshinery.core.e2e.base;

import io.github.askmeagain.meshinery.core.EnableMeshinery;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@EnableMeshinery
@SpringBootApplication
@Import(E2eTestConfiguration.class)
public class E2eTestApplication {

  public static final int NUMBER_OF_TOPICS = 5;
  public static final int ITEMS = 300;
  public static final int SLEEP_IN_PROCESSOR = 5;

  public static final Map<String, Boolean> RESULT_MAP_0 = new ConcurrentHashMap<>();
  public static final Map<String, Boolean> RESULT_MAP_1 = new ConcurrentHashMap<>();
  public static final Map<String, Boolean> RESULT_MAP_2 = new ConcurrentHashMap<>();
  public static final Map<String, Boolean> RESULT_MAP_3 = new ConcurrentHashMap<>();

  public static final String TOPIC_PREFIX = "TOPIC_";

}
