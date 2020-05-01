package io.apef.base.config.factory;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public abstract class XConfigBaseX<T extends XConfigBaseX<T>> extends XConfigBase {
    private Extensions extensions=new Extensions();
    @Getter(AccessLevel.NONE)
    private ExtensionListener<Extension<T>> extensionListener;

    protected XConfigBaseX() {

    }

    protected XConfigBaseX(String name) {
        super(name);
    }

    public XConfigBaseX setExtensions(Extensions extensions) {
        this.extensions = extensions;
        this.extensions.extensionListener =
                extension -> {
                    if (this.extensionListener != null)
                        this.extensionListener.handle(newExtension(extension));
                };
        if (!extensions.isEmpty()) {
            extensions.forEach(s -> {
                Stream.of(s.split(",")).forEach(s1 -> {
                    if (this.extensionListener != null) {
                        this.extensionListener.handle(newExtension(s1));
                    }
                });
            });
        }
        return this;
    }

    protected T setExtensionListener(ExtensionListener<Extension<T>> extensionListener) {
        this.extensionListener = extensionListener;

        if(this.extensions!=null){
            this.extensions.extensionListener =
                    extension -> {
                        if (this.extensionListener != null)
                            this.extensionListener.handle(newExtension(extension));
                    };
            if(!this.extensions.isEmpty()){
                extensions.forEach(s -> this.extensionListener.handle(newExtension(s)));
            }
        }
        return (T) this;
    }

    private T copyAsName(String name) {
        T copied = this.copy();
        copied.setName(name);
        return copied;
    }

    private Extension<T> newExtension(String name){
        return new Extension<T>(name) {
            @Override
            public T extension() {
                return copyAsName(name);
            }
        };
    }

    protected abstract T copy();

    public static abstract class Extension<T extends XConfigBaseX<T>> {
        private String name;

        protected Extension(String name){
            this.name=name;
        }

        public String getName(){
            return this.name;
        }

        public abstract T extension();
    }

    public interface ExtensionListener<T> {
        void handle(T extension);
    }

    public static class Extensions extends ArrayList<String> {
        private ExtensionListener<String> extensionListener;

        @Override
        public boolean add(String s) {
            if(s==null) return super.add("");
            Stream.of(s.split(",")).forEach(s1 -> {
                if (this.extensionListener != null) {
                    this.extensionListener.handle(s1);
                }
            });
            return super.add(s);
        }

        @Override
        public String set(int index, String s) {
            Stream.of(s.split(",")).forEach(s1 -> {
                if (this.extensionListener != null) {
                    this.extensionListener.handle(s1);
                }
            });
            return super.set(index, s);
        }

        @Override
        public String get(int index) {
            return super.get(index);
        }

        @Override
        public void add(int index,String s) {
            if(s==null){
                super.add(index,"");
                return;
            };
            Stream.of(s.split(",")).forEach(s1 -> {
                if (this.extensionListener != null) {
                    this.extensionListener.handle(s1);
                }
            });
            super.add(index, s);
        }
    }
}
