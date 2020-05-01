package io.apef.testing.utils;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;

public class XmlPropertySourceLoader implements PropertySourceLoader, PriorityOrdered {
    @Override
    public String[] getFileExtensions() {
        return new String[]{"xml"};
    }

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        Properties properties = Xml2Properties.load(resource.getInputStream());
        if (!properties.isEmpty()) {
            return Collections.singletonList(new PropertiesPropertySource(name, properties));
        }

        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
