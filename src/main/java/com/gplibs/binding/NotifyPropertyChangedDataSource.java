package com.gplibs.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

abstract class NotifyPropertyChangedDataSource implements INotifyPropertyChangedDataSource, Serializable {

    private final WeakSet<IPropertyChangedListener> mListeners = new WeakSet<>();

    @Override
    public void addListener(IPropertyChangedListener listener) {
        synchronized(mListeners) {
            mListeners.add(listener);
        }
    }

    @Override
    public void removeListener(IPropertyChangedListener listener) {
        synchronized(mListeners) {
            mListeners.remove(listener);
        }
    }

    @Override
    public abstract Object getProperty(String propertyName);

    void onPropertyChanged(String propertyName, Object oldValue, Object newValue) {
        List<IPropertyChangedListener> t;
        synchronized(mListeners) {
            t = new ArrayList<>(mListeners);
        }
        for (IPropertyChangedListener listener : t) {
            if (listener != null) {
                listener.onPropertyChanged(this, propertyName, oldValue, newValue);
            }
        }
    }

}
