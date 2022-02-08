package com.minelittlepony.mson.compatibility.ragdoll;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;

import net.diebuddies.physics.PhysicsEntity;
import net.diebuddies.physics.ragdoll.Ragdoll;
import net.diebuddies.physics.ragdoll.RagdollHook;
import net.diebuddies.physics.ragdoll.RagdollMapper;
import net.fabricmc.api.ClientModInitializer;

import com.minelittlepony.mson.impl.skeleton.Skeleton;
import com.minelittlepony.mson.impl.skeleton.SkeletonisedModel;

import java.util.List;
import java.util.Map;

public class MsonRagdollHook implements RagdollHook, ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RagdollMapper.addHook(this);
    }

    @Override
    public void map(Ragdoll ragdoll, Entity entity, @SuppressWarnings("rawtypes") EntityModel model) {

        if (model instanceof SkeletonisedModel) {
            Skeleton skeleton = ((SkeletonisedModel)model).getSkeleton();
            if (skeleton != null) {
                Map<ModelPart, Integer> idLookup = GeometryDumper.INSTANCE.dumpGeometry(entity);

                skeleton.traverse((parent, child) -> {
                    if (parent.visible && child.visible) {
                        int parentId = idLookup.getOrDefault(parent, -1);
                        int childId = idLookup.getOrDefault(child, -1);
                        if (parentId >= 0 && childId >= 0 && parentId < ragdoll.bodies.size() && childId < ragdoll.bodies.size()) {
                            ragdoll.addConnection(parentId, childId);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void filterCuboidsFromEntities(List<PhysicsEntity> blockifiedEntity, Entity entity, @SuppressWarnings("rawtypes") EntityModel model) {
    }
}
