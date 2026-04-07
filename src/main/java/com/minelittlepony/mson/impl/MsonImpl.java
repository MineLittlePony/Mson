package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;

import net.minecraft.resources.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.impl.key.AbstractModelKeyImpl;
import com.minelittlepony.mson.impl.model.bbmodel.BBModelFormat;
import com.minelittlepony.mson.impl.model.json.MsonModelFormat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class MsonImpl implements Mson {
    public static final Logger LOGGER = LogManager.getLogger("Mson");
    public static final MsonImpl INSTANCE = new MsonImpl();

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath("mson", name);
    }

    private final PendingEntityRendererRegistry renderers = new PendingEntityRendererRegistry();

    final Map<Identifier, ModelKey<?>> registeredModels = new HashMap<>();

    final Map<Identifier, ModelFormat<?>> formatHandlers = new HashMap<>();
    final Map<String, Set<ModelFormat<?>>> handlersByExtension = new HashMap<>();

    final AtomicReference<ModelFoundry> foundry = new AtomicReference<>(new ModelFoundry(this));

    public final MsonModelReloadManager reloadManager = new MsonModelReloadManager(this);

    private MsonImpl() {
        registerModelFormatHandler(ModelFormat.MSON, MsonModelFormat.INSTANCE);
        registerModelFormatHandler(ModelFormat.BBMODEL, BBModelFormat.INSTANCE);
    }

    public Stream<ModelFormat<?>> getHandlers(String extension) {
        return handlersByExtension.getOrDefault(extension, Set.of()).stream();
    }

    @Override
    public PendingEntityRendererRegistry getEntityRendererRegistry() {
        return renderers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Model<?>> ModelKey<T> registerModel(Identifier id, MsonModel.Factory<T> constructor) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(constructor, "Implementation class must not be null");
        checkNamespace(id.getNamespace());
        Preconditions.checkArgument(!registeredModels.containsKey(id), "A model with the id `%s` was already registered", id);

        return (ModelKey<T>)registeredModels.computeIfAbsent(id, _ -> new AbstractModelKeyImpl.Reference<>(id, foundry, constructor));
    }

    public static void checkNamespace(String namespace) {
        Preconditions.checkArgument(!"minecraft".equalsIgnoreCase(namespace), "Id must have a namespace other than `minecraft`.");
        Preconditions.checkArgument(!"mson".equalsIgnoreCase(namespace), "`mson` is a reserved namespace.");
        Preconditions.checkArgument(!"dynamic".equalsIgnoreCase(namespace), "`dynamic` is a reserved namespace.");
    }

    @Override
    public ModelFormat<JsonElement> getDefaultFormatHandler() {
        return MsonModelFormat.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Data, T extends ModelFormat<Data>> T registerModelFormatHandler(Identifier id, T format) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(format, "Format must not be null");
        Objects.requireNonNull(format.getFileExtension(), "Format must have a valid, non-null, non-empty file extension");
        Preconditions.checkArgument(!format.getFileExtension().isEmpty(), "Format must have a valid, non-null, non-empty file extension");
        Preconditions.checkArgument(!format.getFileExtension().startsWith("."), "Extension must not have a leading decimal (.)");
        if (formatHandlers.containsKey(id)) {
            LOGGER.warn("A format handler with id `{}`and extension {} and has already been registered.", id, formatHandlers.get(id).getFileExtension());
            return (T)formatHandlers.get(id);
        }
        formatHandlers.put(id, format);
        handlersByExtension.computeIfAbsent(format.getFileExtension(), _ -> new HashSet<>()).add(format);
        return format;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Data> Optional<ModelFormat<Data>> getFormatHandler(Identifier id) {
        return Optional.ofNullable((ModelFormat<Data>)formatHandlers.get(id));
    }
}
