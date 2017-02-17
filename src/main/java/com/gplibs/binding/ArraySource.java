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
        int i = Integer.parseInt(propertyName.substring(1, propertyName.length() - 1));
        if (mArray.getClass().isArray()) {
            return Array.get(mArray, i);
        } else {
            return null;
        }
    }

}