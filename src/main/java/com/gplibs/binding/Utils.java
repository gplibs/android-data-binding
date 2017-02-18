package com.gplibs.binding;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

class Utils {

    private static final Class[] sBaseClasses = new Class[] {
            byte.class, char.class, short.class, int.class, long.class, float.class, double.class, boolean.class
    };

    private static final Class[] sBaseClasses1 = new Class[] {
            Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class
    };

    static Field getField(Object obj, String fieldName) {
        Field[] fields = obj.getClass().getDeclaredFields();
        Field field = null;
        for (Field f : fields) {
            BindingField bf = f.getAnnotation(BindingField.class);
            if (bf != null && fieldName.equals(bf.value())) {
                field = f;
                break;
            }
        }
        if (field == null) {
            field = getSerializedField(obj, fieldName);
        }
        if (field == null) {
            try {
                field = obj.getClass().getDeclaredField(fieldName);
            } catch (Exception ex) {
                if (BindingManager.isDebug()) {
                    ex.printStackTrace();
                }
            }
        }
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
    }

    static Object getFieldValue(Object object, String fieldName) {
        try {
            Field f = getField(object, fieldName);
            if (f != null) {
                f.setAccessible(true);
                return f.get(object);
            }
            return null;
        } catch (Exception ex) {
            if (BindingManager.isDebug()) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    static boolean isSubClass(Type subClass, Type parentClass) {
        if (subClass == null || parentClass == null) {
            return false;
        }

        if (subClass.equals(parentClass)) {
            return true;
        }

        if (subClass instanceof Class) {
            if (isSubClass(((Class) subClass).getSuperclass(), parentClass)) {
                return true;
            }
            Class[] cs = ((Class) subClass).getInterfaces();
            for (Class c : cs) {
                if (isSubClass(c, parentClass)) {
                    return true;
                }
            }
        }

        return false;
    }

    static boolean isBaseType(Class clazz) {
        return indexOf(sBaseClasses, clazz) != -1 || indexOf(sBaseClasses1, clazz) != -1;
    }

    static Class getSameBaseType(Class baseType) {
        int index = indexOf(sBaseClasses, baseType);
        if (index != -1){
            return sBaseClasses1[index];
        }
        index = indexOf(sBaseClasses1, baseType);
        if (index != -1){
            return sBaseClasses[index];
        }
        return null;
    }

    static boolean equals(Object obj, Object obj1) {
        return (obj == null && obj1 == null) || (obj != null && obj.equals(obj1));
    }

    static Integer getArrayIndex(String propertyName) {
        if (TextUtils.isEmpty(propertyName)) {
            return null;
        }
        propertyName = propertyName.trim();
        if (propertyName.matches("^\\[\\d+\\]$")) {
            return Integer.parseInt(propertyName.substring(1, propertyName.length() - 1));
        } else {
            return null;
        }
    }

    private static Field getSerializedField(Object obj, String fieldName) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            SerializedName sn = f.getAnnotation(SerializedName.class);
            if (sn != null && fieldName.equals(sn.value())) {
                return f;
            }
        }
        return null;
    }

    private static int indexOf(Class[] classes, Class clazz) {
        int index = 0;
        for (Class c : classes) {
            if (c == clazz) {
                return index;
            }
            ++index;
        }
        return -1;
    }

}
