package ask.me.again.meshinery.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/metrics")
public class MonitoringApiController {

  @GetMapping("/prometheus")
  public String prometheus() {
    var result = MeshineryMonitoringService.getMetrics();
    var headers = new HttpHeaders();
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return result;
  }
}
