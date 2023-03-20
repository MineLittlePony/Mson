package com.minelittlepony.mson.impl;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.CowEntityRenderer;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EndermanEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.ModelView;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.model.biped.MsonPlayer;

import java.util.function.Function;

final class Test {
    static void init() {
        /*MsonImpl.DEBUG = true;
        var STEVE = playerRendererFactor(Mson.getInstance().registerModel(new Identifier("mson", "steve"), MsonPlayer::new));
        var ALEX = playerRendererFactor(Mson.getInstance().registerModel(new Identifier("mson", "alex"), MsonPlayer::new));
        MsonImpl.DEBUG = false;*/

        //var RAYMAN = playerRendererFactor(Mson.getInstance().registerModel(new Identifier("mson_test", "rayman"), MsonPlayer::new));
        //var PLANE = playerRendererFactor(Mson.getInstance().registerModel(new Identifier("mson_test", "plane"), MsonPlayer::new));

        var RAYMAN = playerRendererFactor(Mson.getInstance().registerModel(new Identifier("mson_test", "slot_test"), MsonPlayer::new));

        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("default", RAYMAN);
        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("slim", RAYMAN);

        if (true) return;

        ModelKey<CreeperEntityModel<CreeperEntity>> CREEPER = Mson.getInstance().registerModel(new Identifier("mson_test", "creeper"), CreeperEntityModel::new);
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.CREEPER, r -> new CreeperEntityRenderer(r) {{
            this.model = CREEPER.createModel();
        }});

        ModelKey<PigEntityModel<PigEntity>> PIG = Mson.getInstance().registerModel(new Identifier("mson_test", "pig"), PigEntityModel::new);
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.PIG, r -> new PigEntityRenderer(r) {{
            this.model = PIG.createModel();
        }});

        ModelKey<CowEntityModel<CowEntity>> COW = Mson.getInstance().registerModel(new Identifier("mson_test", "cow"), CowEntityModel::new);
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.COW, r -> new CowEntityRenderer(r) {{
            this.model = COW.createModel();
        }});

        ModelKey<EndermanEntityModel<EndermanEntity>> ENDERMAN = Mson.getInstance().registerModel(new Identifier("mson_test", "enderman"), EndermanEntityModel::new);
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.ENDERMAN, r -> new EndermanEntityRenderer(r) {{
            this.model = ENDERMAN.createModel();
        }});
    }

    static Function<EntityRendererFactory.Context, PlayerEntityRenderer> playerRendererFactor(ModelKey<?> key) {
        return r -> new PlayerEntityRenderer(r, false) {{
            this.model = key.createModel();
        }};
    }

    public static class Slot implements MsonModel {

        public Slot(ModelPart tree) {
            System.out.println(tree.getChild("test"));
        }

        @Override
        public void init(ModelView context) {
            System.out.println(context.getLocalValue("a_local", 0));
        }
    }
}
