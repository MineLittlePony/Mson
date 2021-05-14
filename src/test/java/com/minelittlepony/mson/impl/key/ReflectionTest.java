package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;

import java.util.function.Function;

public final class ReflectionTest {

    @SuppressWarnings("unchecked")
    public static void init() {
        MethodHandles.lookupConstructor(Function.class, PublicConst.class, ModelPart.class).apply(null);
        MethodHandles.lookupConstructor(Function.class, PrivateConst.class, ModelPart.class).apply(null);

        MethodHandles.lookupConstructor(Function.class, PublicConst.class, ModelContext.class).apply(null);
        MethodHandles.lookupConstructor(Function.class, PrivateConst.class, ModelContext.class).apply(null);
    }

    public static class PublicConst implements MsonModel {
        public PublicConst(ModelPart root) {
            System.out.println("Public worked");
        }
        public PublicConst(ModelContext root) {
            System.out.println("Public worked (context)");
        }
    }
    private static class PrivateConst implements MsonModel {
        private PrivateConst(ModelPart root) {
            System.out.println("Private worked");
        }
        private PrivateConst(ModelContext root) {
            System.out.println("Private worked (context)");
        }
    }
}
