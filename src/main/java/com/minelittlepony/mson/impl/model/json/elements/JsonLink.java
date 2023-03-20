package com.minelittlepony.mson.impl.model.json.elements;

import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.parser.ModelComponent;

import java.util.concurrent.ExecutionException;

/**
 * A direct link to another named element in a model.
 *
 * When used, the linked model's produced object will appear in the place of this link, wherever it may be.
 * An example use case is to allow for easier overriding of deeply nested elements.
 *
 *
 * @author Sollace
 */
 /* Eg.
 *
 * parent.json
 * {
 *  "data": {
 *      "head": {
 *          ...
 *                "children": {
 *                  "left_ear": "#root_left_ear"
 *                }
 *          ...
 *      },
 *      "root_left_ear": {
 *          ...
 *      }
 *  }
 * }
 *
 * child.json
 * {
 *  "parent": "mson:parent",
 *  "data": {
 *      "root_left_ear": {
 *           ...  Own custom definition for the left ear.
 *                This will appear instead of the parent's left ear, in the original head
 *      }
 *  }
 * }
 */
public class JsonLink implements ModelComponent<Object> {

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
