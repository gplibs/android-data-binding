package com.gplibs.binding;

public interface IAsyncValueConverter {
    void convert(Object sourceValue, IValueConverterCallback callback);
}
