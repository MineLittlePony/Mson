package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelKey;

public abstract class AbstractModelKeyImpl<T extends Model> implements ModelKey<T> {

    protected Identifier id;

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ModelKey && ((ModelKey<?>)other).getId().equals(getId());
    }
}
