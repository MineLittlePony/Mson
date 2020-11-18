package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.model.MsonPart;

import java.util.Random;

public class MsonPlayer<T extends LivingEntity>
    extends PlayerEntityModel<T>
    implements MsonModel {

    protected ModelPart cape;
    protected ModelPart deadmsEars;

    private boolean empty;

    public MsonPlayer(ModelPart tree) {
        super(tree, false);
        cape = tree.getChild("cloak");
        deadmsEars = tree.getChild("ear");
        empty = tree.traverse().filter(p -> !p.isEmpty()).count() == 0;
    }

    @Override
    public ModelPart getRandomPart(Random random) {
        if (empty) {
            return MsonPart.EMPTY_PART;
        }
        return super.getRandomPart(random);
    }

    public static class Renderer extends PlayerEntityRenderer {
        public Renderer(EntityRendererFactory.Context context, ModelKey<MsonPlayer<AbstractClientPlayerEntity>> key) {
            super(context, false);
            this.model = key.createModel();
        }
    }
}
