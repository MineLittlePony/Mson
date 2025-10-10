package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface Vert {
    default Vector3fc getPos() {
        return new Vector3f(((ModelPart.Vertex)this).x(), ((ModelPart.Vertex)this).y(), ((ModelPart.Vertex)this).z());
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
        Vector3f pos = new Vector3f(((ModelPart.Vertex)this).x(), ((ModelPart.Vertex)this).y(), ((ModelPart.Vertex)this).z()).rotate(rotation);
        return new ModelPart.Vertex(pos.x, pos.y, pos.z, getU(), getV());
    }
}
