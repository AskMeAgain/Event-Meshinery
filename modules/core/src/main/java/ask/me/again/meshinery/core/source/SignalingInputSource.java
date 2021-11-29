package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SignalingInputSource<K, C extends Context> implements InputSource<K, C> {

  @Getter
  private final String name;
  private final InputSource<K, C> signalingInputSource;
  private final InputSource<K, C> innerInputSource;

  @Override
  public List<C> getInputs(K key) {

    var signal = signalingInputSource.getInputs(key);

    if (signal.isEmpty()) {
      return Collections.emptyList();
    }

    return innerInputSource.getInputs(key);
  }
}
