package com.gplibs.binding;

import android.text.TextUtils;

import com.gplibs.binding.IDataSource;

import java.lang.reflect.Array;

class ArraySource implements IDataSource {

    private Object mArray;

    ArraySource(Object array) {
        mArray = array;
    }

    @Override
    public Object getProperty(String propertyName) {
        if (TextUtils.isEmpty(propertyName)) {
            return mArray;
        }
        Integer i = Utils.getArrayIndex(propertyName);
        if (mArray == null || i == null) {
            return null;
        }
        if (mArray.getClass().isArray()) {
            return Array.get(mArray, i);
        } else {
            return null;
        }
    }

}
