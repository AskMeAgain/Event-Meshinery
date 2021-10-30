package ask.me.again.meshinery.core.common.sources;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import lombok.Builder;
import lombok.Singular;

import java.util.Collections;
import java.util.List;

@Builder
public class TestInputSource<T extends Context> implements InputSource<String, T> {

  @Singular
  List<T> todos;

  boolean flag;

  @Override
  public List<T> getInputs(String key) {
    if (flag) {
      return Collections.emptyList();
    }
    flag = true;

    return todos;
  }
}
