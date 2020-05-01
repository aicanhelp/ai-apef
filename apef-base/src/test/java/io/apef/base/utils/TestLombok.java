package io.apef.base.utils;


import io.apef.testing.unit.BaseUnitSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.testng.annotations.Test;

public class TestLombok extends BaseUnitSpec {

    @Data
    public static class ConfigBase {
        private String name = "test";

        @Override
        public String toString() {
            return ObjectFormatter.toString(this);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Config1 extends ConfigBase {
        private String type;

        @Override
        //test show: in order to output 'type', the method toString must be overrided.
        public String toString() {
            return ObjectFormatter.toString(this);
        }
    }

    @Test
    public void test() {
        Config1 config1 = new Config1();
        //log.info(config1.toString());

    }

    public  class Base<T extends Base<T>> {
        public T set(){
            return (T)this;
        }
    }

    public  class A<T extends A<T>> extends Base<T>{
        public T setA(){
            return (T)this;
        }
    }

    public  class B<T extends B<T>> extends A<T>{
        public  T setB(){
            return (T)this;
        }
    }

}
