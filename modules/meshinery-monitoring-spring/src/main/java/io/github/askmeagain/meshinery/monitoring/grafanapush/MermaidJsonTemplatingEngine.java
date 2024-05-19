package io.github.askmeagain.meshinery.monitoring.grafanapush;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@RequiredArgsConstructor
public class MermaidJsonTemplatingEngine {

  private final InputStream mermaidTemplate;
  private final MeshineryPushProperties properties;
  private final ObjectMapper objectMapper;
  private final List<MeshineryTask<?, ?>> tasks;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void sendDashboardToGrafana() {
    var body = fillTemplate();

    var restTemplate = new RestTemplateBuilder()
        .basicAuthentication(properties.getUsername(), properties.getPassword())
        .build();

    var entity = new HttpEntity<>(body);

    restTemplate.postForEntity(properties.getGrafanaUrl(), entity, String.class);
    log.info("Pushed new Dashboard '{}' to Grafana at '{}'", properties.getDashboardName(), properties.getGrafanaUrl());
  }

  @SneakyThrows
  private String fillTemplate() {
    var tree = objectMapper.readTree(mermaidTemplate);

    var dashboard = tree.path("dashboard");
    ((ObjectNode) dashboard).put("title", properties.getDashboardName());

    var panelsNode = dashboard.path("panels").get(0);
    var dataSourceUidNode = (ObjectNode) panelsNode.path("datasource");
    dataSourceUidNode.put("uid", properties.getDataSourceUid());

    var panelsNode2 = dashboard.path("panels").get(1);
    var dataSourceUidNode2 = (ObjectNode) panelsNode2.path("datasource");
    dataSourceUidNode2.put("uid", properties.getDataSourceUid());

    var panelsNode3 = dashboard.path("panels").get(2);
    var dataSourceUidNode3 = (ObjectNode) panelsNode3.path("datasource");
    dataSourceUidNode3.put("uid", properties.getDataSourceUid());

    ((ObjectNode) panelsNode).put("title", properties.getDashboardName());
    var contentUrl = panelsNode.path("options");

    ((ObjectNode) contentUrl).put("contentUrl", properties.getMermaidDiagramUrl());

    var targets2 = (ArrayNode) panelsNode2.path("targets");
    var targets3 = (ArrayNode) panelsNode3.path("targets");

    var obj = targets2.addObject();
    var datasource = obj.putObject("datasource");
    datasource.put("type", "prometheus");
    datasource.put("uid", properties.getDataSourceUid());

    var obj2 = targets3.addObject();
    var datasource2 = obj2.putObject("datasource");
    datasource2.put("type", "prometheus");
    datasource2.put("uid", properties.getDataSourceUid());

    var targets = (ArrayNode) panelsNode.path("targets");

    tasks.forEach(task -> addMetric(targets, task.getTaskName(), properties.getMetricQuery(), "currently_processed_"));

    return tree.toPrettyString();
  }

  private void addMetric(ArrayNode targets, String taskName, String metricQuery, String id) {
    var obj = targets.addObject();

    var datasource = obj.putObject("datasource");
    datasource.put("type", "prometheus");
    datasource.put("uid", properties.getDataSourceUid());

    obj.put("editorMode", "builder");
    obj.put("exemplar", false);
    obj.put("expr", metricQuery.replace("$taskName", taskName) + " OR on() vector(0)");
    obj.put("instant", false);
    obj.put("legendFormat", taskName);
    obj.put("range", true);
    obj.put("refId", id + taskName);
  }
}
