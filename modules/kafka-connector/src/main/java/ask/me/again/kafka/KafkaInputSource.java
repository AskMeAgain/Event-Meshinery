package ask.me.again.kafka;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.InputSource;

import java.util.List;

public class KafkaInputSource <C extends Context> implements InputSource<String, C> {

  @Override
  public List<C> getInputs(String key) {
    return null;
  }
}
