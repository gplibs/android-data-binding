package com.gplibs.binding;

public interface IPropertyChangedListener {
    void onPropertyChanged(Object source, String propertyName, Object oldValue, Object newValue);
}
