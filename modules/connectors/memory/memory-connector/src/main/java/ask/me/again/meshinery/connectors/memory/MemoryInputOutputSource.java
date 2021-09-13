package ask.me.again.meshinery.connectors.memory;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryInputOutputSource<K, C extends Context> implements InputSource<K, C>, OutputSource<K, C> {

    private final ConcurrentHashMap<K, C> map = new ConcurrentHashMap<>();

    @Override
    public List<C> getInputs(K key) {
        var list = new ArrayList<C>();
        list.add(map.remove(key));
        return list;
    }

    @Override
    public void writeOutput(K key, C output) {
        map.put(key, output);
    }
}
