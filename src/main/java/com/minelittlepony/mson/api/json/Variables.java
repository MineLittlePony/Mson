package com.minelittlepony.mson.api.json;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.util.Incomplete;

/**
 * Interface for accessing contextual information in the JsonContext.
 * Returns mostly Incomplete<>'s which have to be resolved against the ModelContext at construction time.
 */
public interface Variables {

    /**
     * Reads a json member into an incomplete holding a unresolved integer array.
     * Variables in the array are resolved against the model context when requested.
     */
    Incomplete<int[]> getInts(JsonObject json, String member, int len);

    /**
     * Reads a json member into an incomplete holding a unresolved float array.
     * Variables in the array are resolved against the model context when requested.
     */
    Incomplete<float[]> getFloats(JsonObject json, String member, int len);
}
