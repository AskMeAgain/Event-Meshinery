package io.github.askmeagain.meshinery.core;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final ObjectMapper objectMapper;

  @SneakyThrows
  @PostMapping("/{taskName}")
  public <O extends DataContext> ResponseEntity<DataContext> injectContext(
      @PathVariable("taskName") String taskName,
      @RequestBody String context
  ) {
    var body = taskReplayFactory.<O>injectData(taskName, (O)objectMapper.convertValue(context, Object.class));
    return ResponseEntity.ok(body);
  }

}
