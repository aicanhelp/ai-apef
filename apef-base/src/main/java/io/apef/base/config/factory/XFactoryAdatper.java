package io.apef.base.config.factory;


public class XFactoryAdatper<T,
        C extends XConfigBase> extends XFactoryBase<T, C> {
    private Newer<T, C> newer;
    private Closer<T> closer;

    public XFactoryAdatper(XFactoryConfig<C> factoryConfig, Newer<T, C> newer, Closer<T> closer) {
        super(factoryConfig);
        this.newer = newer;
        this.closer = closer;
    }

    @Override
    protected T newInstance(C config) throws Exception {
        if (this.newer == null) return null;
        return this.newer.create(config);
    }

    @Override
    protected void close(T instance) {
        if (this.closer == null) return;
        this.closer.close(instance);
    }

    public interface Newer<T, C extends XConfigBase> {
        T create(C config) throws Exception;
    }

    public interface Closer<T> {
        void close(T instance);
    }
}
