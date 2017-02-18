package com.gplibs.binding;

import com.gplibs.binding.IDataSource;

import java.util.Collection;
import java.util.List;

public class CollectionSource implements IDataSource {

    private Collection<?> mCollection;

    CollectionSource(Collection<?> collection) {
        mCollection = collection;
    }

    @Override
    public Object getProperty(String propertyName) {
        Integer i = Utils.getArrayIndex(propertyName);
        if (mCollection == null || i == null) {
            return null;
        }
        if (mCollection instanceof List) {
            return ((List)mCollection).get(i);
        } else {
            return mCollection.toArray()[i];
        }
    }
}
