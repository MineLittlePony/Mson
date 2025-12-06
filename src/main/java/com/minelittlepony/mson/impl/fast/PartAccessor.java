package com.minelittlepony.mson.impl.fast;

import net.minecraft.client.model.geom.ModelPart;

import com.minelittlepony.mson.api.model.traversal.PartSkeleton;

import java.util.List;

public interface PartAccessor extends PartSkeleton {
    List<ModelPart.Cube> getCuboids();

    @Deprecated
    @Override
    default int getTotalDirectCubes() {
        return getCuboids().size();
    }
}
