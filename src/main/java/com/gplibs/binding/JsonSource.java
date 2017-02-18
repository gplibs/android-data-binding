package com.gplibs.binding;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.gplibs.binding.IDataSource;

class JsonSource implements IDataSource {

    private JsonElement mJson;

    JsonSource(String json) {
        mJson = new JsonParser().parse(json);
    }

    JsonSource(JsonElement json) {
        mJson = json;
    }

    @Override
    public Object getProperty(String propertyName) {
        if (TextUtils.isEmpty(propertyName)) {
            return getValue(mJson);
        }
        Object p = convertPropertyName(propertyName);
        JsonElement j = null;
        if (p instanceof Integer && mJson.isJsonArray()) {
            JsonArray a = mJson.getAsJsonArray();
            if (a != null) {
                j = a.get((Integer) p);
            }
        } else if (mJson.isJsonObject()) {
            JsonObject o = mJson.getAsJsonObject();
            if (o != null) {
                j = o.get(propertyName);
            }
        }
        return getValue(j);
    }

    private Object getValue(JsonElement j) {
        if (j != null && !j.isJsonNull()) {
            if (j.isJsonArray() || j.isJsonObject()) {
                return j;
            }
            JsonPrimitive jp = j.getAsJsonPrimitive();
            if(jp.isBoolean()) {
                return jp.getAsBoolean();
            } else if (jp.isNumber()) {
                Double d = jp.getAsDouble();
                long l = d.longValue();
                if (l == d) {
                    if (l > Integer.MAX_VALUE) {
                        return l;
                    } else {
                        return (int)l;
                    }
                } else {
                    if (d > Float.MAX_VALUE) {
                        return d;
                    } else {
                        return (float)d.doubleValue();
                    }
                }
            } else {
                return jp.getAsString();
            }
        } else {
            return null;
        }
    }

    private Object convertPropertyName(String field) {
        Integer i = Utils.getArrayIndex(field);
        return i == null ? field : i;
    }
}
