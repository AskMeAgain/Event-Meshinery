package ask.me.again.core;

public interface ReactiveProcessor<C extends Context> {

  C process(C context);
}
