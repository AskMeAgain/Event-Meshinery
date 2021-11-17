package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.source.MemoryConnector;
import ask.me.again.meshinery.draw.MeshineryDrawer;
import ask.me.again.meshinery.draw.MeshineryDrawerConfiguration;
import ask.me.again.meshinery.example.entities.VotingContext;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static ask.me.again.meshinery.example.config.ExampleVoteConfiguration.REST_SIGNAL_IN;

@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleController {

  private final MemoryConnector<String, VotingContext> voteOutputSource;
  private final MeshineryDrawer meshineryDrawer;

  @PostMapping("vote")
  public void uservote(@RequestBody VotingContext voteContext) {
    log.info("Received Vote with id: '{}' and result '{}'", voteContext.getId(), voteContext.isApproved());
    voteOutputSource.writeOutput(REST_SIGNAL_IN, voteContext);
  }

  @GetMapping("graph")
  public ResponseEntity<ByteArrayResource> graph() throws IOException {
    return MeshineryDrawerConfiguration.picture(meshineryDrawer);
  }

  @GetMapping("graph/{subgraph}")
  public ResponseEntity<ByteArrayResource> graph(@PathVariable("subgraph") String subgraph) throws IOException {
    return MeshineryDrawerConfiguration.picture(meshineryDrawer, subgraph);
  }
}
