package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.*;
import net.minecraft.util.math.Direction;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.mixin.Lambdas;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.impl.invoke.MethodHandles;
import com.minelittlepony.mson.util.Qbit;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 *
 */
@SuppressWarnings("unchecked")
public final class BoxBuilder {

    private static final Function<Rect[], ?> RECT_ARR_CAST;
    private static final Function<Vert[], ?> VERT_ARR_CAST;

    private static final Rect.Factory RECT_FACTORY;
    private static final Vert.Factory VERT_FACTORY;

    private static final BiConsumer<Cuboid, Object> POLY_SETTER;

    static {
        final Class<?> Rect = MethodHandles.findHiddenInnerClass(ModelPart.class, Rect.class);
        final Class<?> Vert = MethodHandles.findHiddenInnerClass(ModelPart.class, Vert.class);

        RECT_ARR_CAST = MethodHandles.createArrayCast(Rect);
        VERT_ARR_CAST = MethodHandles.createArrayCast(Vert);

        Lambdas lambdas = MethodHandles.lambdas().remap(Vert.class, Vert).remap(Rect.class, Rect);

        RECT_FACTORY = lambdas.lookupFactory(Rect.Factory.class, Rect, Rect.ConstrDefinition.class);
        VERT_FACTORY = lambdas.lookupFactory(Vert.Factory.class, Vert);
        POLY_SETTER = (BiConsumer<Cuboid, Object>)(Object)lambdas.lookupSetter(Cuboid.class, Rect[].class, "sides");
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

    public float stretchX;
    public float stretchY;
    public float stretchZ;

    public boolean mirrorX;
    public boolean mirrorY;
    public boolean mirrorZ;

    public BoxBuilder(ModelContext context) {
        part = (MsonPart)context.getContext();

        stretchX = context.getScale();
        stretchY = context.getScale();
        stretchZ = context.getScale();

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

    public BoxBuilder stretch(float... stretch) {
        this.stretchX =+ stretch[0];
        this.stretchY =+ stretch[1];
        this.stretchZ =+ stretch[2];
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
            int y, int height,
            Direction direction,
            Vert ...vertices) {
        return quad(x, width, y, height, direction, part.getMirrorX(), vertices);
    }

    /**
     * Creates a new quad with the given spatial vertices.
     */
    public Rect quad(
            int x, int width,
            int y, int height,
            Direction direction,
            boolean mirror,
            Vert ...vertices) {
        return RECT_FACTORY.create(
                VERT_ARR_CAST.apply(vertices),
                x,         y,
                x + width, y + height,
                u, v,
                mirror,
                direction);
    }

    public Cuboid build() {
        return new Cuboid(
                u, v,
                x, y, z,
                dx, dy, dz,
                stretchX, stretchY, stretchZ,
                part.getMirrorX(),
                part.getTexture().getWidth(), part.getTexture().getHeight());
    }

    public Cuboid build(QuadsBuilder builder) {
        Cuboid box = build();
        POLY_SETTER.accept(box, RECT_ARR_CAST.apply(builder.build(this)));
        return box;
    }

    public interface ContentAccessor {
        List<Cuboid> cubes();

        List<ModelPart> children();
    }
}
