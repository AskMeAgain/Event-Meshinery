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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    voteOutputSource.writeOutput("user-vote", voteContext);
  }

  @GetMapping("graph")
  public ResponseEntity<ByteArrayResource> graph() throws IOException {
    return MeshineryDrawerConfiguration.picture(meshineryDrawer);
  }
}
