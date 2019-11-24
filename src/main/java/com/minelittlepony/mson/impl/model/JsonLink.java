package com.minelittlepony.mson.impl.model;

import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import java.util.concurrent.ExecutionException;

public class JsonLink implements JsonComponent<Object> {

    private final String linkName;

    public JsonLink(String name) {
        if (!name.startsWith("#")) {
            throw new JsonParseException("link name should begin with a `#`.");
        }

        linkName = name.substring(1);
    }

    @Override
    public Object export(ModelContext context) throws InterruptedException, ExecutionException {
        return context.computeIfAbsent(linkName, context::findByName);
    }
}
