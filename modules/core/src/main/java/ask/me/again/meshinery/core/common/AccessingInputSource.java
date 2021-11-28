package ask.me.again.meshinery.core.common;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface AccessingInputSource<K, C extends Context> extends InputSource<K, C> {

  Optional<C> getContext(K key, String id);

}
