package io.apef.base.utils;

public interface TestInterface<T extends TestInterface<T>> extends Comparable<T> {
    int MAX_ID = 31;
    int MAX_USER_ID = 23;

    /**
     * Custom id Value is from 0~23,
     * 24~31 is for other
     *
     * @return
     */
    int id();

    /**
     * For Test only.
     *
     * @return
     */
    static <T extends TestInterface<T>> TestInterface<T> newType(int id) {
        return () -> id;
    }

    static <T extends TestInterface<T>> TestInterface<T> newType() {
        return () -> 0;
    }

    default int compareTo(T o) {
        if (o == null) return 1;
        if (this.id() == o.id()) return 0;
        if (this.id() < o.id()) return -1;
        return 1;
    }
}