package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.util.PartUtil;

public class MsonPlayer<T extends LivingEntity>
    extends PlayerModel
    implements MsonModel {

    private boolean empty;

    public MsonPlayer(ModelPart tree) {
        super(tree, false);
        empty = tree.getAllParts().stream().noneMatch(p -> !p.isEmpty());
    }

    @Override
    public ModelPart getRandomBodyPart(RandomSource random) {
        if (empty) {
            return PartUtil.EMPTY_PART;
        }
        return super.getRandomBodyPart(random);
    }
}
