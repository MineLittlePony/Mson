package com.minelittlepony.mson.impl;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.ModelView;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.export.VanillaModelSerializer;
import com.minelittlepony.mson.api.model.biped.MsonPlayer;
import com.minelittlepony.mson.api.parser.ModelLoader;
import com.minelittlepony.mson.impl.key.AbstractModelKeyImpl;
import com.minelittlepony.mson.impl.model.bbmodel.BBModelFormat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class Test {
    static void init() {
        var ID = new Identifier("mson_test", "planar_cube");
        var RAYMAN = playerRendererFactor(Mson.getInstance().registerModel(ID, MsonPlayer::new));
        //var PLANE = playerRendererFactor(Mson.getInstance().registerModel(new Identifier("mson_test", "plane"), MsonPlayer::new));

        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer(ID, player -> true, RAYMAN);
    }

    static void exportVanillaModels(ModelLoader modelLoader) {
        try (var serializer = new VanillaModelSerializer(modelLoader)) {
            serializer.exportAll(FabricLoader.getInstance().getGameDir().resolve("debug_model_export").resolve("vanilla").normalize());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static void exportBbModels(Iterable<? extends AbstractModelKeyImpl<?>> keys) {
        Path basePath = FabricLoader.getInstance().getGameDir().resolve("debug_model_export").resolve("bbmodels");
        List<Exception> exceptions = new ArrayList<>();
        BBModelFormat.INSTANCE.createSerializer().ifPresent(serializer -> {
            try (serializer) {
                keys.forEach(key -> {
                    key.getModelData().ifPresent(content -> {
                        try {
                            serializer.writeToFile(
                                    basePath.resolve(key.getId().getNamespace()).resolve(key.getId().getPath() + ".bbmodel").normalize(),
                                    content
                            );
                        } catch (IOException e) {
                            exceptions.add(new JsonParseException("Could not write model " + key.getId(), e));
                        }
                    });
                });
            } catch (Exception ex) {
                exceptions.add(ex);
            } finally {
                if (!exceptions.isEmpty()) {
                    AssertionError error = new AssertionError("Aggregate Exception");
                    exceptions.forEach(error::addSuppressed);
                    throw error;
                }
            }
        });
    }

    static Function<EntityRendererFactory.Context, PlayerEntityRenderer> playerRendererFactor(ModelKey<? extends PlayerEntityModel<AbstractClientPlayerEntity>> key) {
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
            assert context.getLocalValue("a_local", 0) == 1F;
            System.out.println(context.getLocalValue("a_local", 0));
        }
    }
}
