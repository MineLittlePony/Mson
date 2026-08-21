package com.minelittlepony.mson.impl.fast;

import net.minecraft.client.model.geom.ModelPart;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.util.PartUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Sodium's renders are too basic. Do it properly.
 */
public class FastModelPart extends ModelPart {
    static final float SCALE_FACTOR = 1/16F;

    private boolean compiled;

    private boolean empty;
    private ModelPart[] parts;

    private ResettableVertex[] vertices;
    private ResettableVertex[] normals;
    private Fragment[] fragments;

    private Set<String> warnedPartNames;

    public FastModelPart(List<Cube> cuboids, Map<String, ModelPart> children, float[] rotate, float[] pivot, boolean hidden) {
        super(cuboids, children);
        setRotation(rotate[0], rotate[1], rotate[2]);
        setPos(pivot[0], pivot[1], pivot[2]);
        setInitialPose(storePose());
        visible = !hidden;
    }

    @Override
    public ModelPart getChild(final String name) {
        if (!hasChild(name)) {
            if (warnedPartNames == null) {
                warnedPartNames = new HashSet<>();
            }
            if (warnedPartNames.add(name)) {
                MsonImpl.LOGGER.warn("Model does not contain required named part: " + name);
            }
            return PartUtil.EMPTY_PART;
        }
        return super.getChild(name);
    }

    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        computeContents();

        if (!visible || empty) {
            return;
        }

        matrices.pushPose();
        translateAndRotate(matrices);
        if (!skipDraw) {
            PoseStack.Pose entry = matrices.last();
            fastRenderCuboids(entry, vertices, light, overlay, color);
        }
        for (ModelPart modelPart : parts) {
            modelPart.render(matrices, vertices, light, overlay, color);
        }
        matrices.popPose();
    }

    private void computeContents() {
        if (compiled) {
            return;
        }
        PartAccessor accessor = (PartAccessor)this;
        empty = accessor.getCuboids().isEmpty() && accessor.getChildren().isEmpty();
        this.parts = accessor.getChildren().values().toArray(ModelPart[]::new);

        Map<Vector3f, Vector3f> vertices = new HashMap<>();
        Map<Vector3f, Vector3f> normals = new HashMap<>();
        List<Fragment> fragments = new ArrayList<>();
        for (com.minelittlepony.mson.api.model.Cube cube : accessor.getCuboids()) {
            for (int i = 0; i < cube.sideCount(); i++) {
                Polygon quad = (Polygon)cube.getSide(i);
                for (Vertex vert : quad.vertices()) {
                    fragments.add(new Fragment(
                        vert.u(), vert.v(),
                        vertices.computeIfAbsent(new Vector3f(vert.getPos()), Function.identity()),
                        normals.computeIfAbsent(new Vector3f(quad.normal()), Function.identity())
                    ));
                }
            }
        }
        this.fragments = fragments.toArray(Fragment[]::new);
        this.vertices = vertices.keySet().stream().map(vert -> new ResettableVertex(vert, new Vector3f(vert).mul(SCALE_FACTOR))).toArray(ResettableVertex[]::new);
        this.normals = normals.keySet().stream().map(norm -> new ResettableVertex(norm, new Vector3f(norm))).toArray(ResettableVertex[]::new);
        compiled = true;
    }

    private void fastRenderCuboids(PoseStack.Pose entry, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        Matrix4f positionMatrix = entry.pose();
        Matrix3f normalMatrix = entry.normal();
        Vector4f position = new Vector4f();

        for (ResettableVertex vertex : vertices) {
            vertex.transformPosition(positionMatrix, position);
        }
        for (ResettableVertex norm : normals) {
            norm.transformNormal(normalMatrix);
        }
        for (Fragment frag : fragments) {
            var pos = frag.pos();
            var norm = frag.norm();
            vertexConsumer.addVertex(pos.x, pos.y, pos.z, color, frag.u(), frag.v(), overlay, light, norm.x, norm.y, norm.z);
        }
    }

    private record ResettableVertex(Vector3f vertex, Vector3f pos) {
        void transformPosition(Matrix4f matrix, Vector4f rotationSpace) {
            matrix.transform(rotationSpace.set(pos, 1));
            vertex.set(rotationSpace.x(), rotationSpace.y(), rotationSpace.z());
        }

        void transformNormal(Matrix3f matrix) {
            matrix.transform(vertex.set(pos));
        }
    }

    private record Fragment(float u, float v, Vector3f pos, Vector3f norm) {}
}
