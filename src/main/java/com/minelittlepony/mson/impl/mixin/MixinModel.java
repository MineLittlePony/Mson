package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

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
    protected @Final Function<Identifier, RenderType> renderType;

    @Override
    public Function<Identifier, RenderType> getRenderLayerFactory() {
        return renderType;
    }

    @Override
    public void setRenderLayerFactory(Function<Identifier, RenderType> supplier) {
        renderType = supplier;
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
