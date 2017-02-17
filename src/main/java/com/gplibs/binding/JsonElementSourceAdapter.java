package com.gplibs.binding;

import com.google.gson.JsonElement;

import java.lang.reflect.Type;

class JsonElementSourceAdapter implements IDataSourceAdapter<JsonElement> {

    @Override
    public IDataSource getDataSource(JsonElement data) {
        return new JsonSource(data);
    }

    @Override
    public Type typeOfData() {
        return JsonElement.class;
    }
}
