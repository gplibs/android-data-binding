package com.gplibs.binding;

import android.text.TextUtils;

import com.gplibs.binding.NotifyPropertyChangedDataSource;
import com.gplibs.binding.Utils;

import java.util.Map;

public class MapSource extends NotifyPropertyChangedDataSource {

    private Map mMap;

    public MapSource(Map map) {
        mMap = map;
    }

    public void put(String key, Object value) {
        Object ov = null;
        if (mMap.containsKey(key)) {
            ov = mMap.get(key);
        }
        if (Utils.equals(ov, value)) {
            return;
        }
        mMap.put(key, value);
        onPropertyChanged(key, ov, value);
    }

    public void remove(String key) {
        if (mMap.containsKey(key)) {
            Object ov = mMap.get(key);
            mMap.remove(key);
            onPropertyChanged(key, ov, null);
        }
    }

    @Override
    public Object getProperty(String propertyName) {
        if (TextUtils.isEmpty(propertyName)) {
            return mMap;
        }
        return mMap.get(propertyName);
    }

}
