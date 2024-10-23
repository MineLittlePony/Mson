package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface Vert {
    default Vector3f getPos() {
        return ((ModelPart.Vertex)this).pos();
    }

    default float getU() {
        return ((ModelPart.Vertex)this).u();
    }

    default float getV() {
        return ((ModelPart.Vertex)this).v();
    }

    default Vert rotate(float x, float y, float z) {
        return rotate(new Quaternionf().rotateXYZ(x, y, z));
    }

    default Vert rotate(Quaternionf rotation) {
        getPos().rotate(rotation);
        return this;
    }
}
