package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.random.Random;

import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.util.PartUtil;

public class MsonPlayer<T extends LivingEntity>
    extends PlayerEntityModel
    implements MsonModel {

    private boolean empty;

    public MsonPlayer(ModelPart tree) {
        super(tree, false);
        empty = tree.traverse().stream().noneMatch(p -> !p.isEmpty());
    }

    @Override
    public ModelPart getRandomPart(Random random) {
        if (empty) {
            return PartUtil.EMPTY_PART;
        }
        return super.getRandomPart(random);
    }
}
