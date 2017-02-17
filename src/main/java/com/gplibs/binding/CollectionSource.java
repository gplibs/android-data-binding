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
        if (mCollection == null || !propertyName.matches("\\[\\d+\\]")) {
            return null;
        }
        int i = Integer.parseInt(propertyName.substring(1, propertyName.length() - 1));
        if (mCollection instanceof List) {
            return ((List)mCollection).get(i);
        } else {
            return mCollection.toArray()[i];
        }
    }
}
