package ask.me.again.meshinery.core.common;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class GraphData<K> {
  @Getter
  List<K> inputKeys = new ArrayList<>();
  @Getter
  List<K> outputKeys = new ArrayList<>();

  public GraphData(List<K> inputKeys, List<K> outputKeys) {
    this.inputKeys = inputKeys;
    this.outputKeys = outputKeys;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public GraphData<K> addInputKey(K key) {
    List<K> outputKeys = new ArrayList<>(getOutputKeys());
    List<K> inputKeys = new ArrayList<>(getInputKeys());
    inputKeys.add(key);
    return new GraphData<>(inputKeys, outputKeys);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public GraphData<K> addOutputKey(K key) {
    List<K> outputKeys = new ArrayList<>(getOutputKeys());
    List<K> inputKeys = new ArrayList<>(getInputKeys());
    outputKeys.add(key);
    return new GraphData<>(inputKeys, outputKeys);
  }
}
