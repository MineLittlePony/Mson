package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartBuilder {

    Texture texture = Texture.EMPTY;

    @Deprecated
    float[] offset = new float[3];

    private float[] rotate = new float[3];
    private float[] pivot = new float[3];

    boolean[] mirror = new boolean[3];

    final List<ModelPart.Cuboid> cubes = new ArrayList<>();
    final Map<String, ModelPart> children = new HashMap<>();

    boolean hidden;

    public PartBuilder addChild(String name, ModelPart child) {
        children.put(name, child);
        return this;
    }

    public PartBuilder addCube(ModelPart.Cuboid cube) {
        cubes.add(cube);
        return this;
    }

    public PartBuilder hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    /**
     * Sets the cuboid's texture parameters.
     */
    public PartBuilder tex(Texture tex) {
        this.texture = tex;
        return this;
    }

    public PartBuilder rotate(float... rotation) {
        this.rotate = rotation;
        return this;
    }

    public PartBuilder pivot(float... pivot) {
        this.pivot = pivot;
        return this;
    }

    /**
     * Sets whether certain dimensions are mirrored.
     */
    public PartBuilder mirror(boolean... mirror) {
        this.mirror = mirror;
        return this;
    }

    /**
     * Sets an offset to be used on all shapes and children created through this renderer.
     */
    @Deprecated
    public PartBuilder offset(float... offset) {
        this.offset = offset;
        return this;
    }

    public ModelPart build() {
        ModelPart part = new ModelPart(cubes, children);
        part.setAngles(rotate[0], rotate[1], rotate[2]);
        part.setPivot(pivot[0], pivot[1], pivot[2]);
        part.visible = !hidden;
        return part;
    }

}
