package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.mson.api.model.BoxBuilder.RenderLayerSetter;
import com.minelittlepony.mson.api.model.traversal.SkeletonisedModel;
import com.minelittlepony.mson.api.model.traversal.Traversable;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(Model.class)
abstract class MixinModel implements Consumer<ModelPart>, RenderLayerSetter, SkeletonisedModel {
    @Nullable
    private Traversable<ModelPart> mson_skeleton;

    @Shadow @Mutable
    protected @Final Function<Identifier, RenderLayer> layerFactory;
    @Accessor("layerFactory") @Override
    public abstract Function<Identifier, RenderLayer> getRenderLayerFactory();
    @Override
    public void setRenderLayerFactory(Function<Identifier, RenderLayer> supplier) {
        layerFactory = supplier;
    }
    @Override
    public Traversable<ModelPart> getSkeleton() {
        return mson_skeleton;
    }
    @Override
    public void setSkeleton(Traversable<ModelPart> skeleton) {
        this.mson_skeleton = skeleton;
    }
}
