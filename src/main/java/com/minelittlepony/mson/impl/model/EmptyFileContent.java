package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.exception.EmptyContextException;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.traversal.Traversable;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.parser.FileContent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class EmptyFileContent implements FileContent<Object>, FileContentLocalsImpl {

    public static FileContent<?> INSTANCE = new EmptyFileContent();

    private EmptyFileContent() {}

    @Override
    public ModelFormat<Object> getFormat() {
        return Mson.getInstance().getFormatHandler(ModelFormat.MSON).get();
    }

    @Override
    public Locals getLocals() {
        return this;
    }

    @Override
    public Identifier getModelId() {
        return EmptyModelContext.ID;
    }

    @Override
    public CompletableFuture<FileContent<?>> resolve(Object data) {
        throw new EmptyContextException("resolve");
    }

    @Override
    public CompletableFuture<Set<String>> getComponentNames() {
        return CompletableFuture.completedFuture(new HashSet<>());
    }

    @Override
    public CompletableFuture<Set<String>> keys() {
        return getComponentNames();
    }

    @Override
    public <T> void addNamedComponent(String name, ModelComponent<T> component) {
    }

    @Override
    public <T> Optional<ModelComponent<T>> loadComponent(String name, Object data, Identifier defaultAs) {
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Optional<ModelComponent<?>>> getComponent(String name) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public Optional<Traversable<String>> getSkeleton() {
        return Optional.empty();
    }

    @Override
    public ModelContext createContext(Model model, Object thisObj, ModelContext.Locals locals) {
        return EmptyModelContext.INSTANCE;
    }

    @Override
    public CompletableFuture<Texture> getTexture() {
        return CompletableFuture.completedFuture(Texture.EMPTY);
    }

    @Override
    public CompletableFuture<Incomplete<Float>> getLocal(String name, float defaultValue) {
        return CompletableFuture.completedFuture(Incomplete.completed(defaultValue));
    }

    @Override
    public CompletableFuture<float[]> getDilation() {
        return CompletableFuture.completedFuture(new float[] { 0, 0, 0 });
    }
}
