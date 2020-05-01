package io.apef.base.config.spring;

import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.validation.DataBinder;

import java.beans.PropertyEditor;
import java.util.Map;


public class CustomPropertiesConfigurationFactory<T> extends PropertiesConfigurationFactory<T> {

    private Map<Class, PropertyEditor> propertyEditors;

    public CustomPropertiesConfigurationFactory(T target) {
        super(target);
    }

    public CustomPropertiesConfigurationFactory(T target, Map<Class, PropertyEditor> propertyEditors) {
        super(target);
        this.propertyEditors = propertyEditors;
    }

    @Override
    protected void customizeBinder(DataBinder dataBinder) {
        super.customizeBinder(dataBinder);
        if (this.propertyEditors != null) {
            this.propertyEditors.forEach(dataBinder::registerCustomEditor);
        }
    }
}
