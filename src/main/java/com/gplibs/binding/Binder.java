package com.gplibs.binding;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.JsonElement;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class Binder {

    private WeakReference<Object> mSource;
    private WeakReference<Object> mTarget;
    private String mSourceFieldPath;
    private String mTargetFieldName;
    private Object mConverter;
    private final WeakHashMap<INotifyPropertyChangedDataSource, String> mNotifyPropertyData = new WeakHashMap<>();
    private List<Method> mTargetMethods;
    private Field mTargetField;
    private Object mSourceValue;
    private Object mConvertValue = null;
    static final CopyOnWriteArrayList<IDataSourceAdapter> sDataSourceAdapters = new CopyOnWriteArrayList<>();
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private final Runnable mUpdateTargetRunnable = new Runnable() {
        @Override
        public void run() {
            setTargetValue();
        }
    };

    private IPropertyChangedListener propertyChangedListener = new IPropertyChangedListener() {
        @Override
        public void onPropertyChanged(Object sourceObj, final String propertyName, final Object oldValue, final Object newValue) {
            for (INotifyPropertyChangedDataSource k : mNotifyPropertyData.keySet()) {
                if (mNotifyPropertyData.get(k).equals(propertyName) && sourceObj == k) {
                    mSourceValue = null;
                    updateSourceData();
                    updateTarget();
                    break;
                }
            }
        }
    };

    Binder(Object source, Object target, String sourceFieldPath, String targetFieldName, Object converter) {
        this.mSource = new WeakReference<>(source);
        this.mTarget = new WeakReference<>(target);
        this.mSourceFieldPath = sourceFieldPath;
        this.mTargetFieldName = targetFieldName;
        this.mConverter = converter;
        updateSourceData();
        updateTarget();
    }

    void release() {
        clearSourceData();
    }

    private void updateSourceData() {
        clearSourceData();
        Object s = mSource.get();
        if (s != null) {
            mSourceValue = getValue(s, mSourceFieldPath, mNotifyPropertyData);
        }
        for (INotifyPropertyChangedDataSource k : mNotifyPropertyData.keySet()) {
            if (k != null) {
                k.addListener(propertyChangedListener);
            }
        }
    }

    private void updateTarget() {
        if (mTarget.get() == null) {
            return;
        }

        getSourceValue(new IValueConverterCallback() {
            @Override
            public void run(Object result) {
                mConvertValue = result;
                if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                    mUpdateTargetRunnable.run();
                } else {
                    sHandler.post(mUpdateTargetRunnable);
                }
            }
        });
    }

    private void clearSourceData() {
        for (INotifyPropertyChangedDataSource k : mNotifyPropertyData.keySet()) {
            if (k != null) {
                k.removeListener(propertyChangedListener);
            }
        }
        mNotifyPropertyData.clear();
    }

    private void setTargetValue() {
        Object t = mTarget.get();
        if (t == null) {
            return;
        }
        setTargetValue(t, mTargetFieldName, mConvertValue);
        mConvertValue = null;
    }

    private void getSourceValue(IValueConverterCallback callback) {
        try {
            Object v = mSourceValue;
            if (mConverter != null) {
                if (mConverter instanceof IValueConverter) {
                    v = ((IValueConverter) mConverter).convert(v);
                    callback.run(v);
                } else if (mConverter instanceof IAsyncValueConverter) {
                    ((IAsyncValueConverter) mConverter).convert(v, callback);
                } else {
                    callback.run(v);
                }
            } else {
                callback.run(v);
            }
        } catch (Exception ex) {
            if (BindingManager.isDebug()) {
                ex.printStackTrace();
            }
        }
    }

    private void setTargetValue(Object object, String fieldName, Object value) {
        try {
            if (!setTargetValueWithMethod(object, fieldName, value)) {
                setTargetValueWithField(object, fieldName, value);
            }
        } catch (Exception ex) {
            if (BindingManager.isDebug()) {
                ex.printStackTrace();
            }
        }
    }

    private boolean setTargetValueWithMethod(Object object, String fieldName, Object value) {
        if (mTargetMethods == null) {
            String f = String.valueOf(fieldName.charAt(0)).toUpperCase();
            String n = fieldName.length() > 1 ? fieldName.substring(1) : "";
            mTargetMethods = getTargetMethods(object.getClass(), String.format("set%s%s", f, n));
        }
        if (mTargetMethods.size() > 0) {
            Method method = null;
            if (value != null) {
                Method strMethod = null;
                for (Method m : mTargetMethods) {
                    Class pType = m.getParameterTypes()[0];
                    if (Utils.isSubClass(pType, CharSequence.class)) {
                        strMethod = m;
                    }
                    if (Utils.isSubClass(value.getClass(), pType)) {
                        method = m;
                        break;
                    }
                    if (Utils.isBaseType(pType)) {
                        if (value.getClass().equals(Utils.getSameBaseType(pType))) {
                            method = m;
                            break;
                        }
                    }
                }
                if (mConverter == null && method == null && strMethod != null) {
                    method = strMethod;
                    value = getStringValue(value);
                }
            } else {
                for (Method m : mTargetMethods) {
                    Class pType = m.getParameterTypes()[0];
                    if (Utils.isSubClass(pType, CharSequence.class)) {
                        method = m;
                    }
                }
                if (method == null) {
                    method = mTargetMethods.get(0);
                }
            }

            if (method != null) {
                method.setAccessible(true);
                try {
                    method.invoke(object, value);
                } catch (Exception ex) {
                    if (BindingManager.isDebug()) {
                        ex.printStackTrace();
                    }
                }
            }
            return true;
        }
        return false;
    }

    private String getStringValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private boolean setTargetValueWithField(Object object, String fieldName, Object value) {
        if (mTargetField == null) {
            mTargetField = Utils.getField(object, fieldName);
        }
        if (mTargetField != null) {
            try {
                if (Utils.isSubClass(mTargetField.getType(), CharSequence.class)
                        && value != null
                        && !(value instanceof CharSequence)) {
                    value = getStringValue(value);
                }
                mTargetField.setAccessible(true);
                mTargetField.set(object, value);
            } catch (Exception ex) {
                if (BindingManager.isDebug()) {
                    ex.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    private List<Method> getTargetMethods(Class clazz, String methodName) {
        List<Method> methods = new ArrayList<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterTypes().length == 1) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    private Object getValue(Object data, String path, WeakHashMap<INotifyPropertyChangedDataSource, String> outData) {
        if (data == null || TextUtils.isEmpty(path)) {
            return data;
        }
        String[] array = path.split("\\.");
        for (String f : array) {
            if (data instanceof INotifyPropertyChangedDataSource) {
                outData.put((INotifyPropertyChangedDataSource) data, f);
            }
            data = getProperty(data, f);
            if (data == null) {
                return null;
            }
        }
        if (data instanceof JsonElement) {
            JsonElement json = ((JsonElement) data);
            if (json.isJsonPrimitive()) {
                data = json.getAsString();
            } else {
                data = json.toString();
            }
        }
        return data;
    }

    private Object getProperty(Object data, String propertyName) {
        if (data == null) {
            return null;
        }
        if (data instanceof IDataSource) {
            return ((IDataSource) data).getProperty(propertyName);
        } else {
            for (IDataSourceAdapter adapter : sDataSourceAdapters) {
                if (Utils.isSubClass(data.getClass(), adapter.typeOfData())) {
                    IDataSource dataSource = adapter.getDataSource(data);
                    if (dataSource != null) {
                        return dataSource.getProperty(propertyName);
                    }
                }
            }
            if (data.getClass().isArray()) {
                return new ArraySource(data).getProperty(propertyName);
            }
            return Utils.getFieldValue(data, propertyName);
        }
    }

}
