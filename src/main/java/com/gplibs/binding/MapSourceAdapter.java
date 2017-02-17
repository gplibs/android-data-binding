package com.gplibs.binding;

import java.lang.reflect.Type;
import java.util.Map;

class MapSourceAdapter implements IDataSourceAdapter<Map> {
    @Override
    public IDataSource getDataSource(Map data) {
        return new MapSource(data);
    }

    @Override
    public Type typeOfData() {
        return Map.class;
    }
}
