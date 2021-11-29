package ask.me.again.meshinery.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "meshinery.core. shutdown-on-finished=true")
@SpringJUnitConfig(MeshineryAutoConfiguration.class)
class EnabledShutdownHookTest {

  @Autowired
  CustomizeShutdownHook hook;

  @Test
  void testSpringhook() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(hook).isNotNull();
  }

}
