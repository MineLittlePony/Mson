package com.minelittlepony.mson.impl.fast;

import net.minecraft.client.model.geom.ModelPart;

import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.util.PartUtil;

import java.util.Map;
import java.util.TreeMap;

public class UnloadedModelPart extends FastModelPart {

    private final String path;

    private final Map<String, UnloadedModelPart> children = new TreeMap<>();

    public UnloadedModelPart(ModelPart wrapped, String path) {
        super(((PartAccessor)wrapped).getCuboids(), ((PartAccessor)wrapped).getChildren(),
                new float[] { wrapped.xRot, wrapped.yRot, wrapped.zRot },
                new float[] { wrapped.x, wrapped.y, wrapped.z },
                !wrapped.visible
        );
        this.path = path;
    }

    @Override
    public ModelPart getChild(final String name) {
        return children.computeIfAbsent(name, n -> {
            ModelPart child = PartUtil.EMPTY_PART;
            if (!hasChild(name)) {
                MsonImpl.LOGGER.warn("Model does not contain required named part: " + path + "." + n);
            } else {
                child = super.getChild(n);
            }

            return new UnloadedModelPart(child, path + "." + n);
        });
    }
}
