package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.*;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.mixin.Lambdas;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.impl.invoke.MethodHandles;
import com.minelittlepony.mson.util.Qbit;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 *
 */
public final class BoxBuilder {

    private static final Rect.Factory RECT_FACTORY;
    private static final Vert.Factory VERT_FACTORY;
    private static final BiConsumer<Cuboid, Rect[]> POLY_SETTER;

    static {
        final Class<?> Rect = MethodHandles.findHiddenInnerClass(ModelPart.class, Rect.class);
        final Class<?> Vert = MethodHandles.findHiddenInnerClass(ModelPart.class, Vert.class);

        Lambdas lambdas = MethodHandles.lambdas().remap(Vert.class, Vert).remap(Rect.class, Rect);

        RECT_FACTORY = lambdas.lookupFactoryInvoker(Rect.Factory.class, Rect);
        VERT_FACTORY = lambdas.lookupFactoryInvoker(Vert.Factory.class, Vert);
        POLY_SETTER = lambdas.lookupSetter(Cuboid.class, Rect[].class, "sides");
    }

    public final MsonPart part;

    public float x;
    public float y;
    public float z;

    public int dx;
    public int dy;
    public int dz;

    public int u;
    public int v;

    public float stretch;

    public boolean mirrorX;
    public boolean mirrorY;
    public boolean mirrorZ;

    public BoxBuilder(ModelContext context) {
        part = (MsonPart)context.getContext();

        stretch = context.getScale();

        u = part.getTexture().getU();
        v = part.getTexture().getV();

        mirrorX = part.getMirrorX();
        mirrorY = part.getMirrorY();
        mirrorZ = part.getMirrorZ();
    }

    public BoxBuilder pos(float... pos) {
        x = pos[0] + part.getModelOffsetX();
        y = pos[1] + part.getModelOffsetY();
        z = pos[2] + part.getModelOffsetZ();
        return this;
    }

    public BoxBuilder tex(Optional<Texture> tex) {
        tex.ifPresent(t -> {
            u = t.getU();
            v = t.getV();
        });
        return this;
    }

    public BoxBuilder size(int... size) {
        dx = size[0];
        dy = size[1];
        dz = size[2];
        return this;
    }

    public BoxBuilder size(Axis axis, int...dimension) {
        return size(
                axis.getWidth(dimension),
                axis.getHeight(dimension),
                axis.getDeptch(dimension));
    }

    public BoxBuilder stretch(float stretch) {
        this.stretch =+ stretch;
        return this;
    }

    public BoxBuilder mirror(Axis axis, Qbit mirror) {
        if (mirror.isKnown()) {
            if (axis == Axis.X) {
                mirrorX = mirror.toBoolean();
            }
            if (axis == Axis.Y) {
                mirrorY = mirror.toBoolean();
            }
            if (axis == Axis.Z) {
                mirrorZ = mirror.toBoolean();
            }
        }
        return this;
    }

    /**
     * Creates a new vertex mapping the given (x, y, z) coordinates to a texture offset.
     */
    public Vert vert(float x, float y, float z, int u, int v) {
        return VERT_FACTORY.create(x, y, z, u, v);
    }

    /**
     * Creates a new quad with the given spatial vertices.
     */
    public Rect quad(
            int x, int width,
            int y, int height, Vert ...vertices) {
        return RECT_FACTORY.create(vertices,
                x,         y,
                x + width, y + height,
                u, v);
    }

    public Cuboid build() {
        return new Cuboid(
                u, v,
                x, y, z,
                dx, dy, dz,
                stretch, stretch, stretch,
                part.getMirrorX(),
                part.getTexture().getWidth(), part.getTexture().getHeight());
    }

    public Cuboid build(QuadsBuilder builder) {
        Cuboid box = build();
        POLY_SETTER.accept(box, builder.build(this));
        return box;
    }

    public interface ContentAccessor {
        List<Cuboid> cubes();

        List<ModelPart> children();
    }
}
