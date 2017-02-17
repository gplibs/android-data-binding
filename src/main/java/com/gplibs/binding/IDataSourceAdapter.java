package com.gplibs.binding;

import java.lang.reflect.Type;

public interface IDataSourceAdapter<T> {

    IDataSource getDataSource(T data);

    Type typeOfData();
}
