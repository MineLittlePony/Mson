package com.minelittlepony.mson.util;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Maps {
    // Default implementation from java.util.Map
    // is safe and won't throw concurrent modifications on recurse.
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key,
            Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v;
        if ((v = map.get(key)) == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                map.put(key, newValue);
                return newValue;
            }
        }

        return v;
    }
}
