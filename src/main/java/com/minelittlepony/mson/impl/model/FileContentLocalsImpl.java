package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.FileContent.Locals;
import com.minelittlepony.mson.api.parser.locals.LocalBlock;
import com.minelittlepony.mson.impl.ModelLocalsImpl;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface FileContentLocalsImpl extends FileContent.Locals {
    @Override
    default ModelContext.Locals bake() {
        return new ModelLocalsImpl(this);
    }

    @Override
    default Locals extendWith(Identifier modelId, Optional<LocalBlock> locals, Optional<Texture> texture) {
        return union(modelId, this, locals, texture);
    }

    static FileContent.Locals union(Identifier id, FileContent.Locals parent, Optional<LocalBlock> locals) {
        return new Union(id, parent, locals, Optional.empty());
    }

    static FileContent.Locals union(Identifier id, FileContent.Locals parent, Optional<LocalBlock> locals, Optional<Texture> texture) {
        return new Union(id, parent, locals, texture);
    }

    class Union implements FileContentLocalsImpl {
        private final Identifier id;
        private final FileContent.Locals parent;

        private final Optional<LocalBlock> locals;
        private final Optional<Texture> texture;

        Union(Identifier id, FileContent.Locals parent, Optional<LocalBlock> locals, Optional<Texture> texture) {
            this.id = id;
            this.locals = locals;
            this.texture = texture;
            this.parent = parent;
        }

        @Override
        public Identifier getModelId() {
            return id;
        }

        @Override
        public CompletableFuture<float[]> getDilation() {
            return parent.getDilation();
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(parent::getTexture);
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocal(String name, float defaultValue) {
            return locals.flatMap(locals -> locals.get(name)).orElseGet(() -> parent.getLocal(name, defaultValue));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return locals
                    .map(locals -> parent.keys().thenApplyAsync(locals::appendKeys))
                    .orElseGet(() -> parent.keys());
        }
    }
}
