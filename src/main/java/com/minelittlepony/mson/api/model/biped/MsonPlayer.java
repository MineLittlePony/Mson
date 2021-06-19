package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.util.PartUtil;

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
        empty = tree.traverse().noneMatch(p -> !p.isEmpty());
    }

    @Override
    public ModelPart getRandomPart(Random random) {
        if (empty) {
            return PartUtil.EMPTY_PART;
        }
        return super.getRandomPart(random);
    }
}
