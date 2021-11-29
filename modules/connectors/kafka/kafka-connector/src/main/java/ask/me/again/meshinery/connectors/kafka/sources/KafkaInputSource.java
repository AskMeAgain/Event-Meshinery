package ask.me.again.meshinery.connectors.kafka.sources;

import ask.me.again.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.common.InputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class KafkaInputSource<C extends DataContext> implements InputSource<String, C> {

  @Getter
  private final String name;
  private final Class<C> serdeClazz;
  private final ObjectMapper objectMapper;
  private final KafkaConsumerFactory kafkaConsumerFactory;

  @Override
  public List<C> getInputs(String key) {

    var result = kafkaConsumerFactory.get(key)
        .poll(Duration.ofMillis(1000));

    return StreamSupport.stream(result.spliterator(), false)
        .map(ConsumerRecord::value)
        .map(x -> {
          try {
            return objectMapper.readValue(x, serdeClazz);
          } catch (IOException e) {
            e.printStackTrace();
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

}
