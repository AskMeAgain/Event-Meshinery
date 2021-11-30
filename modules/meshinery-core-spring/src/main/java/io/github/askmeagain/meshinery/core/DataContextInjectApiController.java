package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/inject")
@RestController
public class DataContextInjectApiController {

  private final TaskReplayFactory taskReplayFactory;

  @SneakyThrows
  @PostMapping("/{taskName}")
  public ResponseEntity<String> injectContext(
      @PathVariable("taskName") String taskName,
      @RequestBody DataContext context
  ) {

    taskReplayFactory.injectData(taskName, context);

    return ResponseEntity.ok("Inject successful");
  }

}
