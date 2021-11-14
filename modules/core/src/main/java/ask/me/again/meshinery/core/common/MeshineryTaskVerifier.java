package ask.me.again.meshinery.core.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MeshineryTaskVerifier {

  public static List<String> getAndVerifyOutputSources(List<MeshineryTask<?, ?>> tasks) {
    var result = tasks.stream()
        .map(MeshineryTask::getTaskName)
        .toList();

    var duplicates = findDuplicates(result);

    if (duplicates.size() > 0) {
      throw new RuntimeException("Found duplicate job names: [" + String.join(", ", duplicates) + "]");
    }

    return result;
  }


  public static List<String> getAndVerifyInputSources(List<MeshineryTask<?, ?>> tasks) {
    var result = tasks.stream()
        .map(MeshineryTask::getTaskName)
        .toList();

    var duplicates = findDuplicates(result);

    if (duplicates.size() > 0) {
      throw new RuntimeException("Found duplicate job names: [" + String.join(", ", duplicates) + "]");
    }

    return result;
  }

  //https://stackoverflow.com/a/31641116/5563263
  public static List<String> getAndVerifyTaskList(List<MeshineryTask<?, ?>> tasks) {
    var result = tasks.stream()
        .map(MeshineryTask::getTaskName)
        .toList();

    var duplicates = findDuplicates(result);

    if (duplicates.size() > 0) {
      throw new RuntimeException("Found duplicate job names: [" + String.join(", ", duplicates) + "]");
    }

    return result;
  }

  private static <T> Set<T> findDuplicates(Collection<T> collection) {
    Set<T> uniques = new HashSet<>();
    return collection.stream()
        .filter(e -> !uniques.add(e))
        .collect(Collectors.toSet());
  }

}
