package io.apef.base.utils;

import java.util.concurrent.Callable;

/**
 * Singleton
 *
 * @author <a href="mailto:zhuangzhi.liu@thistech.com">Zhuangzhi Liu</a>
 *         Created at 2014/12/29
 */
public class Singleton<T> {
    private T instance;
//    private Callable<T> callable;

    private Singleton(){}

    public Singleton(Callable<T> callable) {
        try {
            this.instance = callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized T getInstance() {
        return instance;
    }
}
