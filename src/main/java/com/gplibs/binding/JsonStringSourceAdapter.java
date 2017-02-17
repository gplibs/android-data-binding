package com.gplibs.binding;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;

class JsonStringSourceAdapter implements IDataSourceAdapter<String> {

    @Override
    public IDataSource getDataSource(String data) {
        JsonElement j = getJson(data);
        if (j != null) {
            return new JsonSource(j);
        }
        return null;
    }

    @Override
    public Type typeOfData() {
        return String.class;
    }

    private static JsonElement getJson(String data) {
        try {
            return new JsonParser().parse(data);
        } catch (Exception ex) {
            if (BindingManager.isDebug()) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
