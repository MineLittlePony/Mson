package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.math.Direction;

import java.util.Set;

public class BoxParameters {
    public final float[] position;
    public final float[] size;
    public float[] dilation;

    public final boolean[] mirror = new boolean[3];

    public Texture uv;

    public BoxParameters(float[] position, float[] size, float[] dilation, Texture uv) {
        this.position = position;
        this.size = size;
        this.dilation = dilation;
        this.uv = uv;
    }

    public BoxParameters() {
        this(new float[3], new float[3], new float[3], Texture.EMPTY);
    }

    public boolean mirrorAny() {
        return mirror[0] || mirror[1] || mirror[2];
    }

    public Cuboid build(PartBuilder parent, Set<Direction> enabledSides) {
        return new Cuboid(
                uv.u(), uv.v(),
                position[0], position[1], position[2],
                size[0], size[1], size[2],
                dilation[0], dilation[1], dilation[2],
                mirror[0],
                parent.texture.width(), parent.texture.height(),
                enabledSides
        );
    }

    public float getBoxFrameUOffset(Direction direction) {
        return switch (direction) {
            case DOWN, NORTH -> size[2];
            case UP, EAST -> size[2] + size[0];
            case WEST -> 0;
            case SOUTH -> size[2] + size[0] + size[2];
        };
    }

    public float getBoxFrameVOffset(Direction direction) {
        return direction == Direction.DOWN ? 0 : size[2];
    }
}
