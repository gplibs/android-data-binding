package com.gplibs.binding;

import android.text.TextUtils;

import com.gplibs.binding.BindingManager;
import com.gplibs.binding.NotifyPropertyChangedDataSource;
import com.gplibs.binding.Utils;

import java.lang.reflect.Field;

public class ModelSource extends NotifyPropertyChangedDataSource {

    @Override
    public Object getProperty(String propertyName) {
        if (TextUtils.isEmpty(propertyName)) {
            return this;
        }
        return Utils.getFieldValue(this, propertyName);
    }

    public void setProperty(String propertyName, Object value) {
        Object ov = getProperty(propertyName);
        if (Utils.equals(ov, value)) {
            return;
        }
        Field f = Utils.getField(this, propertyName);
        if (f != null) {
            try {
                f.set(this, value);
                onPropertyChanged(propertyName, ov, value);
            } catch (Exception ex) {
                if (BindingManager.isDebug()) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
