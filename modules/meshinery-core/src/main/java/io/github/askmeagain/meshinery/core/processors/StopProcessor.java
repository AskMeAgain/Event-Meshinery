package io.github.askmeagain.meshinery.core.processors;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.util.function.Predicate;

/**
 * Processor which takes a predicate. It will return null if the predicate is true, which means the scheduler will
 * stop processing this entry further. Is equivalent to .filter() in Java streams
 *
 * @param <C> Context type
 */
public record StopProcessor<C extends MeshineryDataContext>(Predicate<C> stopIf) implements MeshineryProcessor<C, C> {

  @Override
  public C process(C context) {

    if (stopIf.test(context)) {
      return null;
    }

    return context;
  }
}
