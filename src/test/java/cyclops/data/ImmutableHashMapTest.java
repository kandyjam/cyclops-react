package cyclops.data;

import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class ImmutableHashMapTest extends BaseImmutableMapTest{


    @Override
    protected <K, V> ImmutableMap<K, V> empty() {
        return HashMap.empty();
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return HashMap.of(k1,v1);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        return HashMap.of(k1,v1,k2,v2);
    }

    @Override
    protected ImmutableMap<String, Integer> fromMap(Map<String, Integer> map) {
        Stream<Tuple2<String, Integer>> s = map.entrySet().stream().map(e -> Tuple.tuple(e.getKey(), e.getValue()));
        HashMap<String, Integer> x = HashMap.fromStream(s);
        return x;
    }
}