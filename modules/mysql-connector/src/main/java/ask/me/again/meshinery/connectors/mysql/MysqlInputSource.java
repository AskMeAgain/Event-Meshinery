package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;

import java.util.List;

public class MysqlInputSource<C extends Context> implements InputSource<String, C> {

  @Override
  public List<C> getInputs(String key) {
    return null;
  }
}

