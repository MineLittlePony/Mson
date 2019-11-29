package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.mson.api.model.BoxBuilder.RenderLayerSetter;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(Model.class)
abstract class MixinModel implements Consumer<ModelPart>, RenderLayerSetter {

    @Shadow @Final @Mutable
    protected Function<Identifier, RenderLayer> layerFactory;

    @Override
    @Accessor("layerFactory")
    public abstract Function<Identifier, RenderLayer> getRenderLayerFactory();

    @Override
    public void setRenderLayerFactory(Function<Identifier, RenderLayer> supplier) {
        layerFactory = supplier;
    }
}
