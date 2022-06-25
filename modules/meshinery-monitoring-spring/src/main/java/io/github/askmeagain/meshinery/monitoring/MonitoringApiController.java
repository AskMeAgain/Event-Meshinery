package io.github.askmeagain.meshinery.monitoring;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@RestController
@RequestMapping("/metrics")
public class MonitoringApiController {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @GetMapping("/prometheus")
  public ResponseEntity<String> prometheus() {
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body(MeshineryMonitoringService.getMetrics());
  }
}
