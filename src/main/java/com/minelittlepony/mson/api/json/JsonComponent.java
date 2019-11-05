package com.minelittlepony.mson.api.json;

import net.minecraft.client.model.Cuboid;

import org.apache.commons.lang3.NotImplementedException;

import com.minelittlepony.mson.api.ModelContext;

import java.util.Optional;

public interface JsonComponent<T> {

    @SuppressWarnings("unchecked")
    default <K> Optional<K> tryExport(ModelContext context, Class<K> type) {
        Object s = export(context);

        if (s != null && type.isAssignableFrom(s.getClass())) {
            return Optional.of((K)s);
        }
        return Optional.empty();
    }

    T export(ModelContext context);

    default void export(ModelContext context, Cuboid output) {
        throw new NotImplementedException("I am not a cuboid");
    }
}