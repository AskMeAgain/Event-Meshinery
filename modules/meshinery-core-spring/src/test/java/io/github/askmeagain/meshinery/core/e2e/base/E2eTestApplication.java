package io.github.askmeagain.meshinery.core.e2e.base;

import io.github.askmeagain.meshinery.core.EnableMeshinery;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@EnableMeshinery
@SpringBootApplication
@Import(E2eTestConfiguration.class)
public class E2eTestApplication {

  public static final int NUMBER_OF_TOPICS = 20;
  public static final int ITEMS = 60;
  public static final int THREADS = 30;
  public static final int SLEEP_IN_PROCESSOR = 100;
  public static final HashMap<Integer, List<String>> RESULT_MAP = new HashMap<>();
  public static final String TOPIC_PREFIX = UUID.randomUUID().toString().substring(0, 10);

}
