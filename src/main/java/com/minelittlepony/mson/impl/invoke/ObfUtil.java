package com.minelittlepony.mson.impl.invoke;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

class ObfUtil {

    public static String unmapFieldName(Class<?> ownerType, Class<?> fieldType, String fieldName) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

        return resolver.mapFieldName("named",
                unmapClass(ownerType),
                fieldName,
                unmapClass(fieldType).replace(".", "/")
        );
    }

    public static String unmapClass(Class<?> type) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

        String canon = MethodHandles.getRawClass(type).getName();
        return type.getName().replace(canon, resolver.unmapClassName("named", canon));
    }
}
