package com.gplibs.binding;

import android.support.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArraySet;

public class BindingManager {

    private static boolean sIsDebug = false;
    private static CopyOnWriteArraySet<BindingManager> sManagers = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<Binder> mBinders = new CopyOnWriteArraySet<>();
    private WeakReference<Object> mData;
    private WeakReference<Object> mTarget;

    private BindingManager(Object data, Object target) {
        initAdapters();
        update(data, target);
    }

    private synchronized static void initAdapters() {
        if (Binder.sDataSourceAdapters.size() == 0) {
            Binder.sDataSourceAdapters.add(new CollectionSourceAdapter());
            Binder.sDataSourceAdapters.add(new MapSourceAdapter());
            Binder.sDataSourceAdapters.add(new JsonElementSourceAdapter());
            Binder.sDataSourceAdapters.add(new JsonStringSourceAdapter());
        }
    }

    public static void registerDataSourceAdapter(@NonNull IDataSourceAdapter adapter) {
        initAdapters();
        if (!containsAdapter(adapter)) {
            Binder.sDataSourceAdapters.add(0, adapter);
        }
    }

    public static void unregisterDataSourceAdapter(@NonNull Class<? extends IDataSourceAdapter> clazz) {
        for (IDataSourceAdapter a : Binder.sDataSourceAdapters) {
            if (a.getClass().equals(clazz)) {
                Binder.sDataSourceAdapters.remove(a);
            }
        }
    }

    private static boolean containsAdapter(IDataSourceAdapter adapter) {
        for (IDataSourceAdapter a : Binder.sDataSourceAdapters) {
            if (a.getClass().equals(adapter.getClass())) {
                return true;
            }
        }
        return false;
    }

    public static void setIsDebug(boolean isDebug) {
        sIsDebug = isDebug;
    }

    static boolean isDebug() {
        return sIsDebug;
    }

    public static void binding(Object data, Object target) {
        if (target == null) {
            throw new IllegalArgumentException();
        }

        BindingManager manager = getManager(target);
        if (manager == null) {
            sManagers.add(new BindingManager(data, target));
        } else {
            manager.update(data, target);
        }
        removeReleasedBinding();
    }

    private static void removeReleasedBinding() {
        for (BindingManager m : sManagers) {
            if (m.mTarget.get() == null || m.mData.get() == null) {
                m.release();
                sManagers.remove(m);
            }
        }
    }

    private static BindingManager getManager(Object target) {
        for (BindingManager m : sManagers) {
            if (target.equals(m.mTarget.get())) {
                return m;
            }
        }
        return null;
    }

    private void update(Object data, Object target) {
        mData = new WeakReference<>(data);
        mTarget = new WeakReference<>(target);
        release();
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field f : fields) {
            fieldBinding(f, data, target);
        }
    }

    private void fieldBinding(Field field, Object data, Object target) {
        field.setAccessible(true);
        Object v = null;
        try {
            v = field.get(target);
        } catch (Exception ex) {
            if (BindingManager.isDebug()) {
                ex.printStackTrace();
            }
        }
        if (v == null) {
            return;
        }
        for (Annotation a : field.getDeclaredAnnotations()) {
            if (!(a instanceof Binding) && !(a instanceof ConvertBinding)) {
                continue;
            }
            String targetField;
            String sourceField;
            Object converter = null;
            if (a instanceof Binding) {
                Binding b = (Binding) a;
                targetField = b.target();
                sourceField = b.source();
            } else {
                ConvertBinding b = (ConvertBinding) a;
                targetField = b.target();
                sourceField = b.source();
                converter = getConverter(b.converter());
            }
            mBinders.add(new Binder(data, v, sourceField, targetField, converter));
        }
    }

    private void release() {
        for (Binder b : mBinders) {
            b.release();
        }
        mBinders.clear();
    }

    private Object getConverter(Class<?> clazz) {
        if (!Utils.isSubClass(clazz, IValueConverter.class) && !Utils.isSubClass(clazz, IAsyncValueConverter.class)) {
            throw new IllegalArgumentException();
        }
        try {
            Constructor c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception ex) {
            if (BindingManager.isDebug()) {
                ex.printStackTrace();
            }
            throw new IllegalArgumentException();
        }
    }

}
