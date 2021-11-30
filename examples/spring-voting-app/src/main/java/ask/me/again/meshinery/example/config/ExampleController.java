package ask.me.again.meshinery.example.config;

import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import ask.me.again.meshinery.example.entities.VotingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @PostMapping("vote")
  public void uservote(@RequestBody VotingContext voteContext) {
    log.info("Received Vote with id: '{}' and result '{}'", voteContext.getId(), voteContext.isApproved());
    voteOutputSource.writeOutput(REST_SIGNAL_IN, voteContext);
  }
}
