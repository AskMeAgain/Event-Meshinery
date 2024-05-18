package io.github.askmeagain.meshinery.core.utils.sources;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class OutputCaptureExtension
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

  OutputCaptureExtension() {
    // Package private to prevent users from directly creating an instance.
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    getOutputCapture(context).push();
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    getOutputCapture(context).pop();
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    getOutputCapture(context).push();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    getOutputCapture(context).pop();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return OutputCapture.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return getOutputCapture(extensionContext);
  }

  private OutputCapture getOutputCapture(ExtensionContext context) {
    return getStore(context).getOrComputeIfAbsent(OutputCapture.class);
  }

  private Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass()));
  }

}