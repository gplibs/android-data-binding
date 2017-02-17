package com.gplibs.binding;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class WeakSet<T> implements Set<T> {

    private CopyOnWriteArrayList<WeakReference<T>> mData;

    WeakSet() {
        mData = new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean add(T t) {
        return mData.add(new WeakReference<>(t));
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        for (T o : c) {
            add(o);
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        WeakReference<T> findRef = null;
        for (WeakReference<T> ref : mData) {
            if (o.equals(ref.get())) {
                findRef = ref;
                break;
            }
        }
        return findRef != null && mData.remove(findRef);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        List<WeakReference<T>> findRefs = new ArrayList<>();
        for (WeakReference<T> ref : mData) {
            if (ref.get() == null || c.contains(ref.get())) {
                findRefs.add(ref);
                break;
            }
        }
        for (WeakReference<T> ref : findRefs) {
            mData.remove(ref);
        }
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        List<WeakReference<T>> findRefs = new ArrayList<>();
        for (WeakReference<T> ref : mData) {
            if (ref.get() == null || !c.contains(ref.get())) {
                findRefs.add(ref);
                break;
            }
        }
        for (WeakReference<T> ref : findRefs) {
            mData.remove(ref);
        }
        return false;
    }

    @Override
    public void clear() {
        mData.clear();
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        WeakReference<T> findRef = null;
        for (WeakReference<T> ref : mData) {
            if (o.equals(ref.get())) {
                findRef = ref;
                break;
            }
        }
        return findRef != null;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        for (Object t : c) {
            if (!contains(t)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        removeFinalized();
        return mData.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new WeakListIterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        Object[] r = new Object[size()];
        int i = 0;
        for (WeakReference<T> ref : mData) {
            r[i++] = ref.get();
        }
        return r;
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        int i = 0;
        for (WeakReference<T> ref : mData) {
            if (i >= a.length) {
                break;
            }
            a[i++] = (T1) ref.get();
        }
        return a;
    }

    private void removeFinalized() {
        List<WeakReference<T>> findRefs = new ArrayList<>();
        for (WeakReference<T> ref : mData) {
            if (ref.get() == null) {
                findRefs.add(ref);
                break;
            }
        }
        for (WeakReference<T> ref : findRefs) {
            mData.remove(ref);
        }
    }

    private class WeakListIterator implements Iterator<T> {

        private int size;
        private int index;

        WeakListIterator() {
            size = size();
            index = 0;
        }

        public boolean hasNext() {
            return index < size;
        }

        public T next() {
            return mData.get(index++).get();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
