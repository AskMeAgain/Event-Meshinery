package io.github.askmeagain.meshinery.connectors.kafka.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class KafkaInputSource<C extends MeshineryDataContext>
    implements MeshineryInputSource<String, C>, AutoCloseable {

  @Getter
  private final String name;
  private final Class<C> serdeClazz;
  private final ObjectMapper objectMapper;
  private final KafkaConsumerFactory kafkaConsumerFactory;

  @Override
  @SneakyThrows
  public List<C> getInputs(List<String> keys) {
    var result = kafkaConsumerFactory.get(keys).poll(Duration.ZERO);

    return StreamSupport.stream(result.spliterator(), true)
        .map(ConsumerRecord::value)
        .map(byteArr -> {
          try {
            return objectMapper.readValue(byteArr, serdeClazz);
          } catch (IOException e) {
            log.error("Cannot deserialize object", e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toList();
  }

  @Override
  public void close() {
    kafkaConsumerFactory.close();
  }

  @Override
  public C commit(C context) {
    return context;
  }
}
