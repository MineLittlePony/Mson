package com.minelittlepony.mson.api.model;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.impl.MsonModifyable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartBuilder {

    public Texture texture = Texture.EMPTY;

    public float[] rotate = new float[3];
    public float[] pivot = new float[3];

    public boolean[] mirror = new boolean[3];

    final List<ModelPart.Cuboid> cubes = new ArrayList<>();
    final Map<String, ModelPart> children = new HashMap<>();

    public boolean hidden;

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

    public ModelPart build() {
        ModelPart part = new ModelPart(cubes, children);
        part.setAngles(rotate[0], rotate[1], rotate[2]);
        part.setPivot(pivot[0], pivot[1], pivot[2]);
        part.setDefaultTransform(part.getTransform());
        part.visible = !hidden;
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            ((MsonModifyable)(Object)part).setMsonModified();
        }
        return part;
    }

}
