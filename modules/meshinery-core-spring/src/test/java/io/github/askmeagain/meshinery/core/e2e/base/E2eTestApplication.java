package io.github.askmeagain.meshinery.core.e2e.base;

import io.github.askmeagain.meshinery.core.EnableMeshinery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@EnableMeshinery
@SpringBootApplication
@Import({E2eTestConfiguration.class})
public class E2eTestApplication {

}
