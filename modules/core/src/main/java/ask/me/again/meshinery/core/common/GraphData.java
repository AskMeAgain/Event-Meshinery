package ask.me.again.meshinery.core.common;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;


@SuppressWarnings("checkstyle:MissingJavadocType")
public class GraphData<K> {
  @Getter
  List<K> inputKeys = new ArrayList<>();
  @Getter
  List<K> outputKeys = new ArrayList<>();

}
