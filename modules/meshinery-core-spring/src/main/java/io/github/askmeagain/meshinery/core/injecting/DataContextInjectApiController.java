package io.github.askmeagain.meshinery.core.injecting;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Order(1)
@RequiredArgsConstructor
@RestController
@Conditional(ActivateOnInjection.class)
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

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  @PostConstruct
  public void setup() {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshinery.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshinery.class);
      Arrays.stream(result.injection())
          .forEach(x -> classMap.put(x.getSimpleName(), x));
    }

    for (var fqn : meshineryCoreProperties.getInject()) {
      Class<?> clazz = getClazzFromFullyQualifiedName(fqn);
      classMap.put(clazz.getSimpleName(), clazz);
    }
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  @PostMapping("/inject/{contextType}/{taskName}")
  public ResponseEntity<String> injectContext(
      @PathVariable("taskName") String taskName,
      @PathVariable("contextType") String contextType,
      @RequestBody String context
  ) {
    try {
      var entity = (MeshineryDataContext) objectMapper.readValue(context, classMap.get(contextType));
      var resultBody = taskReplayFactory.injectData(taskName, entity);

      return ResponseEntity.ok(objectMapper.writeValueAsString(resultBody));
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  @PostMapping("/inject/{contextType}/{taskName}/async")
  public ResponseEntity<String> injectContextAsync(
      @PathVariable("taskName") String taskName,
      @PathVariable("contextType") String contextType,
      @RequestBody String context
  ) {
    try {
      var entity = (MeshineryDataContext) objectMapper.readValue(context, classMap.get(contextType));
      taskReplayFactory.injectDataAsync(taskName, entity);

      return ResponseEntity.accepted().body("Accepted");
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  @PostMapping("/replay/{contextType}/{taskName}")
  public ResponseEntity<String> replayContext(
      @PathVariable("taskName") String taskName,
      @PathVariable("contextType") String contextType,
      @RequestBody String context
  ) {
    try {
      var entity = (MeshineryDataContext) objectMapper.readValue(context, classMap.get(contextType));
      taskReplayFactory.replayData(taskName, entity);

      return ResponseEntity.accepted().body("Accepted");
    } catch (Exception ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

  private Class<?> getClazzFromFullyQualifiedName(String fqn) throws ClassNotFoundException {
    try {
      return Class.forName(fqn);
    } catch (ClassNotFoundException e) {
      throw new ClassNotFoundException("Cannot find class {} for injecting preparation.");
    }
  }
}
