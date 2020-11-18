package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;

import com.google.common.base.Strings;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.impl.model.JsonTexture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartBuilder {

    Texture texture = JsonTexture.EMPTY;

    float[] offset = new float[3];

    private float[] rotation = new float[3];
    private float[] pivot = new float[3];

    boolean[] mirror = new boolean[3];

    final List<ModelPart.Cuboid> cubes = new ArrayList<>();
    final Map<String, ModelPart> children = new HashMap<>();

    boolean hidden;

    private String name;

    private final ModelContext context;

    public PartBuilder(ModelContext context) {
        this.context = context;
    }

    public PartBuilder name(String name) {
        this.name = Strings.nullToEmpty(name);
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
        this.rotation = rotation;
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
    public PartBuilder offset(float... offset) {
        this.offset = offset;
        return this;
    }

    public ModelPart build() {
        ModelPart part = new ModelPart(cubes, children);
        ((MsonPart)(Object)part)
            .rotate(rotation)
            .around(pivot)
            .setHidden(hidden);

        if (context.getModel() instanceof PartBuilder) {
            PartBuilder parent = (PartBuilder)context.getModel();

            if (this.name.isEmpty()) {
                this.name = parent.name + ".part" + parent.children.size();
            }
            parent.children.put(name, part);
        }

        return part;
    }

}
