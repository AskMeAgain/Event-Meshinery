package io.github.askmeagain.meshinery.aop.e2e.base;

import io.github.askmeagain.meshinery.aop.EnableMeshineryAop;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMeshineryAop
@SpringBootApplication
@EnableMeshinery(connector = {@EnableMeshinery.KeyDataContext(key = String.class, context = TestContext.class)})
public class E2eAopTestApplication {

}
