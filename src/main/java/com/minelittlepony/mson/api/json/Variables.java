package com.minelittlepony.mson.api.json;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.util.Incomplete;

public interface Variables {

    Incomplete<int[]> getInts(JsonObject json, String member, int len);

    Incomplete<float[]> getFloats(JsonObject json, String member, int len);
}
