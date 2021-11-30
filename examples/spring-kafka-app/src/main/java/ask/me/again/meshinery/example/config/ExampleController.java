package ask.me.again.meshinery.example.config;

import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.draw.MeshineryDrawer;
import ask.me.again.meshinery.example.TestContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleController {

  private final ApplicationContext context;
  private final AtomicBoolean atomicBoolean;
  private final ExecutorService executorService;
  private final KafkaProducerFactory kafkaProducerFactory;
  private final ObjectMapper objectMapper;

  private final List<MeshineryTask<?, ?>> tasks;

  private int counter = 0;

  @GetMapping("shutdown")
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void shutdown() {
    log.info("Gracefully Shutdown");
    atomicBoolean.set(false);
    executorService.shutdown();
    ((ConfigurableApplicationContext) context).close();
  }

  @GetMapping("picture")
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public ResponseEntity picture() throws IOException {
    var result = MeshineryDrawer.builder()
        .tasks(tasks)
        .build()
        .draw();

    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return ResponseEntity.ok()
                         .contentType(MediaType.IMAGE_PNG)
                         .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"test.png\"")
                         .contentLength(result.length)
                         .body(new ByteArrayResource(result));
  }

  @GetMapping("produce/{topic}")
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void insertData(@PathVariable("topic") String topic) throws JsonProcessingException {
    log.info("Inserted Data");

    counter++;

    var output = TestContext.builder()
        .id(counter + "")
        .testValue1(counter)
        .build();
    var key = output.getId();
    var value = objectMapper.writeValueAsBytes(output);

    var record = new ProducerRecord<>(topic, key, value);

    kafkaProducerFactory.get(topic).send(record);
  }
}
