package com.gplibs.binding;

import java.lang.reflect.Type;
import java.util.Collection;

class CollectionSourceAdapter implements IDataSourceAdapter<Collection<?>> {
    @Override
    public IDataSource getDataSource(Collection<?> data) {
        return new CollectionSource(data);
    }

    @Override
    public Type typeOfData() {
        return Collection.class;
    }
}
