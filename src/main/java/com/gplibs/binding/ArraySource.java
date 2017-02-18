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
        if (mArray == null || !propertyName.matches("\\[\\d+\\]")) {
            return null;
        }
        if (mArray.getClass().isArray()) {
            return Array.get(mArray, Integer.parseInt(propertyName.substring(1, propertyName.length() - 1)));
        } else {
            return null;
        }
    }

}
