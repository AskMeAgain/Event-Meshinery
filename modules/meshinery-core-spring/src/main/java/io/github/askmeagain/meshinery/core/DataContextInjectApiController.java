package io.github.askmeagain.meshinery.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/inject")
@RestController
@Lazy
public class DataContextInjectApiController {

  private final TaskReplayFactory taskReplayFactory;
  private final ObjectMapper objectMapper;

  private final Map<String, Class<?>> classMap = new HashMap<>();
  private final ApplicationContext applicationContext;
  private final MeshineryCoreProperties meshineryCoreProperties;

  @ExceptionHandler(value = {Exception.class})
  protected ResponseEntity<Object> handleConflict(Exception ex) {
    return new ResponseEntity<>(ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @SneakyThrows
  @PostConstruct
  void setup() {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshinery.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshinery.class);
      Arrays.stream(result.injection())
          .forEach(x -> classMap.put(x.getSimpleName(), x));
    }

    for (var fqn : meshineryCoreProperties.getInject()) {
      Class<?> clazz = Class.forName(fqn);
      classMap.put(clazz.getSimpleName(), clazz);
    }
  }

  @SneakyThrows
  @PostMapping("/{contextType}/{taskName}")
  public ResponseEntity<String> injectContext(
      @PathVariable("taskName") String taskName,
      @PathVariable("contextType") String contextType,
      @RequestBody String context
  ) {
    try {
      var entity = (DataContext) objectMapper.readValue(context, classMap.get(contextType));
      var resultBody = taskReplayFactory.injectData(taskName, entity);

      return ResponseEntity.ok(objectMapper.writeValueAsString(resultBody));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError()
          .body(ex.getMessage());
    }
  }

  @SneakyThrows
  @PostMapping("/{contextType}/{taskName}/async")
  public ResponseEntity<String> injectContextAsync(
      @PathVariable("taskName") String taskName,
      @PathVariable("contextType") String contextType,
      @RequestBody String context
  ) {
    try {
      var entity = (DataContext) objectMapper.readValue(context, classMap.get(contextType));
      taskReplayFactory.injectDataAsync(taskName, entity);

      return ResponseEntity.accepted().body("Accepted");
    } catch (Exception ex) {
      return ResponseEntity.internalServerError()
          .body(ex.getMessage());
    }
  }
}
