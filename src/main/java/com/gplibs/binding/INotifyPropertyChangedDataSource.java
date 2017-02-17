package com.gplibs.binding;

public interface INotifyPropertyChangedDataSource extends IDataSource {

    void addListener(IPropertyChangedListener listener);

    void removeListener(IPropertyChangedListener listener);
}
