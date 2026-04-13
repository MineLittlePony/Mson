package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.geom.ModelPart;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface Vert {
    default Vector3fc getPos() {
        var self = (ModelPart.Vertex)this;
        return new Vector3f(self.x(), self.y(), self.z());
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
        Vector3f pos = getPos().rotate(rotation, new Vector3f());
        return new ModelPart.Vertex(pos.x, pos.y, pos.z, getU(), getV());
    }

    default Vert remap(float u, float v) {
        return ((ModelPart.Vertex)this).remap(u, v);
    }
}
