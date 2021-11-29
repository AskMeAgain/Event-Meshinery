package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.common.InputSource;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
public class SignalingInputSource<K, C extends DataContext> implements InputSource<K, C> {

  @Getter
  private final String name;
  private final InputSource<K, C> signalingInputSource;
  private final InputSource<K, C> innerInputSource;
  private final K innerKey;

  @Override
  public List<C> getInputs(K key) {

    var signal = signalingInputSource.getInputs(key);

    if (signal.isEmpty()) {
      return Collections.emptyList();
    }

    return innerInputSource.getInputs(innerKey);
  }
}
