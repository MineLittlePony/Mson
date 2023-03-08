package com.minelittlepony.mson.impl;

import net.minecraft.util.Identifier;

import com.google.common.io.Files;

public interface PathUtil {

    static String getExtension(Identifier id) {
        return Files.getFileExtension(id.getPath());
    }

    static String removeExtension(Identifier id) {
        return removeExtension(id.getPath());
    }

    static String removeExtension(String path) {
        int index = path.lastIndexOf('.');
        return index < 0 ? path : path.substring(0, index);
    }
}
