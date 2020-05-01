package io.apef.base.config.utils;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigUtils {
    private static Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    public static <T> T convertFromMap(Class<T> configClass, Map<String, Object> values) {
        T target = null;
        try {
            target = configClass.newInstance();
            ConfigurablePropertyAccessor wrapper = PropertyAccessorFactory.forDirectFieldAccess(target);
            wrapper.setAutoGrowNestedPaths(true);
            wrapper.setPropertyValues(values);
        } catch (Exception ex) {
            log.error("Failed to convert map values to class: " + configClass.getName(), ex);
        }

        return target;
    }

    public static <T> T findFieldByType(Class<T> type, Object target) {
        Field field = FieldUtils.getAllFieldsList(target.getClass()).stream()
                .filter(f -> type == f.getClass())
                .findFirst().orElse(null);
        if (field == null) return null;
        try {
            return (T) FieldUtils.readField(field, target, true);
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> List<T> configs(Class<T> fieldClass, Object target) {
        return configs(fieldClass, target, true);
    }

    public static <T> List<T> configs(Class<T> fieldClass, Object target, boolean includeNull) {
        return FieldUtils.getAllFieldsList(target.getClass()).stream()
                .filter(field -> fieldClass.isAssignableFrom(field.getType()))
                .map(field -> {
                    try {
                        return (T) FieldUtils.readField(field, target, true);
                    } catch (IllegalAccessException ignored) {
                        log.warn("Failed to get field value " + field.getName());
                    }
                    return null;
                }).filter(t -> includeNull || t != null).collect(Collectors.toList());
    }

    public static <T> void update(Class<T> fieldClass, Object target, Changer<T> changer) {
        FieldUtils.getAllFieldsList(target.getClass()).stream()
                .filter(field -> fieldClass.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    try {
                        T currentValue = (T) FieldUtils.readField(field, target, true);
                        FieldUtils.writeField(field, target, changer.change(field.getName(), currentValue), true);
                    } catch (IllegalAccessException ignored) {
                        log.warn("Failed to get field value " + field.getName());
                    }
                });
    }

    public interface Changer<T> {
        T change(String name, T currentValue);
    }
}
