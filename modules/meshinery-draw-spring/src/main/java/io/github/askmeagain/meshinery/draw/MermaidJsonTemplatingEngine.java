package io.github.askmeagain.meshinery.draw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static io.github.askmeagain.meshinery.draw.MeshineryDrawProperties.DashboardPushProperties;

@RequiredArgsConstructor
public class MermaidJsonTemplatingEngine {

  private final InputStream mermaidTemplate;
  private final DashboardPushProperties properties;
  private final ObjectMapper objectMapper;
  private final List<MeshineryTask<?, ?>> tasks;

  public void send() {
    var body = fillTemplate();

    var restTemplate = new RestTemplate();

    HttpHeaders headers = createHeaders(properties.getUsername(), properties.getPassword());

    var entity = new HttpEntity<>(body, headers);

    restTemplate.postForEntity(properties.getGrafanaUrl(), entity, String.class);
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

  @SneakyThrows
  private String fillTemplate() {
    var tree = objectMapper.readTree(mermaidTemplate);

    var dashboard = tree.path("dashboard");
    ((ObjectNode) dashboard).put("title", properties.getDashboardName());

    var panelsNode = dashboard.path("panels").get(0);
    var contentUrl = panelsNode.path("options");

    ((ObjectNode) contentUrl).put("contentUrl", properties.getMermaidDiagramUrl());

    var targets = (ArrayNode) panelsNode.path("targets");

    tasks.forEach(task -> addMetric(targets, task.getTaskName()));

    return tree.toPrettyString();
  }

  private void addMetric(ArrayNode targets, String taskName) {
    var obj = targets.addObject();

    var datasource = obj.putObject("datasource");
    datasource.put("type", "prometheus");
    datasource.put("uid", "PBFA97CFB590B2093");

    obj.put("exemplar", false);
    obj.put("expr", "processing_counter{task_name='" + taskName + "'}");
    obj.put("instant", true);
    obj.put("internal", "");
    obj.put("legendFormat", taskName);
    obj.put("refId", taskName);
  }

}
