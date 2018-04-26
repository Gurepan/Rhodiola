package info.ilambda.rhodiola.core.actor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 这个类不需要多线程下的一致性，只是要保证插入和删除时候的安全，就可以保证不出现fast-fail
 *
 * @param <T>
 */

public class SyncSet<T> implements Set<T> {
    private HashSet<T> ts;

    public SyncSet() {
        ts = new HashSet<>();
    }

    public SyncSet(int i) {
        ts = new HashSet<>(i);
    }

    public SyncSet(Collection<T> t) {
        ts = new HashSet<>(t);
    }

    @Override
    public int size() {
        return ts.size();
    }

    @Override
    public boolean isEmpty() {
        return ts.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return ts.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return ts.iterator();
    }

    @Override
    public Object[] toArray() {
        return ts.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return ts.toArray(a);
    }

    @Override
    public boolean add(T t) {
        synchronized (this) {
            return ts.add(t);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            return ts.remove(o);
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return ts.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        synchronized (this) {
            return ts.addAll(c);
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        synchronized (this) {
            return ts.retainAll(c);
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        synchronized (this) {
            return ts.removeAll(c);
        }
    }

    @Override
    public void clear() {
        ts.clear();
    }
}
