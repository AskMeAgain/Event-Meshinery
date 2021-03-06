package io.github.askmeagain.meshinery.core.injecting;

import io.github.askmeagain.meshinery.core.EnableMeshinery;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class ActivateOnInjection extends AnyNestedCondition {

  ActivateOnInjection() {
    super(ConfigurationPhase.REGISTER_BEAN);
  }

  @Conditional(InjectedActivatedCondition.class)
  static class InjectedActivated { }

  @ConditionalOnProperty(prefix = "meshinery.core", name = "inject")
  static class PropertyFileSet { }

  private static class InjectedActivatedCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      var beanFactory = context.getBeanFactory();

      var beans = Objects.requireNonNull(beanFactory).getBeansWithAnnotation(EnableMeshinery.class);
      for (var tuple : beans.entrySet()) {
        var ann = Objects.requireNonNull(beanFactory.findAnnotationOnBean(tuple.getKey(), EnableMeshinery.class));
        if (ann.injection().length > 0) {
          return true;
        }
      }

      return false;
    }
  }
}