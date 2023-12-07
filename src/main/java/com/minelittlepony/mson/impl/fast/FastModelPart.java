package com.minelittlepony.mson.impl.fast;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.mson.api.model.Cube;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public FastModelPart(List<Cuboid> cuboids, Map<String, ModelPart> children, float[] rotate, float[] pivot, boolean hidden) {
        super(cuboids, children);
        setAngles(rotate[0], rotate[1], rotate[2]);
        setPivot(pivot[0], pivot[1], pivot[2]);
        setDefaultTransform(getTransform());
        visible = !hidden;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        computeContents();

        if (!visible || empty) {
            return;
        }

        matrices.push();
        rotate(matrices);
        if (!hidden) {
            MatrixStack.Entry entry = matrices.peek();
            fastRenderCuboids(entry, vertices, light, overlay, red, green, blue, alpha);
        }
        for (ModelPart modelPart : parts) {
            modelPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
        matrices.pop();
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
        for (Cuboid cube : accessor.getCuboids()) {
            Cube cu = ((Cube)cube);
            for (int i = 0; i < cu.sideCount(); i++) {
                Quad quad = (Quad)cu.getSide(i);
                for (Vertex vert : quad.vertices) {
                    fragments.add(new Fragment(
                        vert.u, vert.v,
                        vertices.computeIfAbsent(vert.pos, Function.identity()),
                        normals.computeIfAbsent(quad.direction, Function.identity())
                    ));
                }
            }
        }
        this.fragments = fragments.toArray(Fragment[]::new);
        this.vertices = vertices.keySet().stream().map(vert -> new ResettableVertex(vert, new Vector3f(vert).mul(SCALE_FACTOR))).toArray(ResettableVertex[]::new);
        this.normals = normals.keySet().stream().map(norm -> new ResettableVertex(norm, new Vector3f(norm))).toArray(ResettableVertex[]::new);
        compiled = true;
    }

    private void fastRenderCuboids(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();
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
            vertexConsumer.vertex(pos.x, pos.y, pos.z, red, green, blue, alpha, frag.u(), frag.v(), overlay, light, norm.x, norm.y, norm.z);
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
