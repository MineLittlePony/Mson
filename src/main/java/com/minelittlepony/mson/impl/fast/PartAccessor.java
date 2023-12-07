package com.minelittlepony.mson.impl.fast;

import net.minecraft.client.model.ModelPart.Cuboid;

import com.minelittlepony.mson.api.model.traversal.PartSkeleton;

import java.util.List;

public interface PartAccessor extends PartSkeleton {
    List<Cuboid> getCuboids();
}
