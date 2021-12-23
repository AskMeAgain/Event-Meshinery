package io.github.askmeagain.meshinery.draw;

import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class MermaidJsonTemplatingEngine {

  private final List<String> mermaidTemplate;
  private final MeshineryDrawProperties properties;

  public void send() {
    var body = fillTemplate();

    var restTemplate = new RestTemplate();
    HttpHeaders headers = createHeaders("admin", "admin");

    var entity = new HttpEntity<>(body, headers);

    restTemplate.postForEntity("http://localhost:3000/api/dashboards/db", entity, String.class);
  }

  //https://www.baeldung.com/how-to-use-resttemplate-with-basic-authentication-in-spring
  HttpHeaders createHeaders(String username, String password) {
    return new HttpHeaders() {{
      String auth = username + ":" + password;
      byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
      String authHeader = "Basic " + new String(encodedAuth);

      set("Authorization", authHeader);
      setContentType(MediaType.APPLICATION_JSON);
    }};
  }

  private String fillTemplate() {
    var builder = new StringBuilder();
    for (var line : mermaidTemplate) {
      builder.append(fillLine(line));
    }

    return builder.toString();
  }

  private String fillLine(String line) {
    var metrics = createMetric("task1") + "," + createMetric("task2") + "," + createMetric("task3");

    return line.replaceAll("\\$\\{CONTENT_URL\\}", "http://10.0.2.15:8080/draw/mermaid")
        .replaceAll("\\$\\{METRICS\\}", metrics);
  }

  private String createMetric(String taskName) {
    return metricTemplate.replaceAll("\\$\\{TASK_NAME\\}", taskName);
  }

  private static final String metricTemplate = """
      {
        "datasource": {
          "type": "prometheus",
          "uid": "PBFA97CFB590B2093"
        },
        "exemplar": false,
        "expr": "processing_counter{task_name='${TASK_NAME}'}",
        "hide": false,
        "instant": true,
        "interval": "",
        "legendFormat": "${TASK_NAME}",
        "refId": "${TASK_NAME}"
      }
      """;
}
