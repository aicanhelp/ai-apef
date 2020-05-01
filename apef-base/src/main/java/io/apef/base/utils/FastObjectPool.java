package io.apef.base.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class FastObjectPool<T> {
    private Holder<T>[] objects;
    private volatile int takePointer;
    private int releasePointer;
    private final int mask;
    private final long BASE;
    private final long INDEXSCALE;
    private final long ASHIFT;
    public ReentrantLock lock = new ReentrantLock();
    private ThreadLocal<Holder<T>> localValue = new ThreadLocal<>();

    public static final Unsafe THE_UNSAFE;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = () -> {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                return (Unsafe) theUnsafe.get(null);
            };
            THE_UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }

    public FastObjectPool(int size) {
        this(null, size);
    }

    @SuppressWarnings("unchecked")
    public FastObjectPool(PoolFactory<T> factory, int size) {
        int newSize = 1;
        while (newSize < size) {
            newSize = newSize << 1;
        }
        size = newSize;
        objects = new Holder[size];
        for (int x = 0; x < size; x++) {
            objects[x] = new Holder<T>(this);
            if (factory != null)
                objects[x].value(factory.create(objects[x]));
        }
        mask = size - 1;
        releasePointer = size;
        BASE = THE_UNSAFE.arrayBaseOffset(Holder[].class);
        INDEXSCALE = THE_UNSAFE.arrayIndexScale(Holder[].class);
        ASHIFT = 31 - Integer.numberOfLeadingZeros((int) INDEXSCALE);
    }

    public Releasable<T> take() {
        return this.take(null);
    }

    public Releasable<T> take(T initValue) {
        int localTakePointer;
        Holder<T> localObject = localValue.get();
        if (localObject != null) {
            if (localObject.state.compareAndSet(Holder.FREE, Holder.USED)) {
                return localObject;
            }
        }
        while (releasePointer != (localTakePointer = takePointer)) {
            int index = localTakePointer & mask;
            Holder<T> holder = objects[index];
//if(holder!=null && THE_UNSAFE.compareAndSwapObject(objects, (index*INDEXSCALE)+BASE, holder, null))
            if (holder != null && THE_UNSAFE.compareAndSwapObject(objects, (index << ASHIFT) + BASE, holder, null)) {
                takePointer = localTakePointer + 1;
                if (holder.state.compareAndSet(Holder.FREE, Holder.USED)) {
                    localValue.set(holder);
                    holder.value(initValue);
                    return holder;
                }
            }
        }
        return null;
    }

    public void release(Holder<T> object) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            int localValue = releasePointer;
//long index = ((localValue & mask) * INDEXSCALE ) + BASE;
            long index = ((localValue & mask) << ASHIFT) + BASE;
            if (object.state.compareAndSet(Holder.USED, Holder.FREE)) {
                THE_UNSAFE.putOrderedObject(objects, index, object);
                releasePointer = localValue + 1;
            } else {
                throw new IllegalArgumentException("Invalid reference passed");
            }
        } finally {
            lock.unlock();
        }
    }

    private static class Holder<T> implements Releasable<T> {
        private T value;
        private FastObjectPool pool;
        public static final int FREE = 0;
        public static final int USED = 1;
        private AtomicInteger state = new AtomicInteger(FREE);

        private Holder(FastObjectPool pool) {
            this.pool = pool;
        }

        private Holder value(T value) {
            this.value = value;
            return this;
        }

        public T value() {
            return value;
        }

        public void release() {
            try {
                this.pool.release(this);
            } catch (Exception ex) {

            }
        }

        @Override
        public void resetValue(T value) {
            this.value = value;
        }
    }

    public interface Releasable<T> {
        void release();

        T value();

        void resetValue(T value);
    }

    public interface PoolFactory<T> {
        T create(Releasable releasable);
    }
}