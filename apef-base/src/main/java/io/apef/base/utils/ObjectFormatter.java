package io.apef.base.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectFormatter {
    private final static Logger log = LoggerFactory.getLogger(ObjectFormatter.class);
    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setVisibility(mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.INDENT_OUTPUT,true)
                .getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    public static String toString(Object obj) {
        if (obj instanceof String) return (String) obj;
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception ex) {
            log.warn("Failed to format object of class: {}, ex: {}", obj.getClass(), ex.getMessage());
            return "";
        }
//        return JSON.toJSONString(obj,
//                (ValueFilter) (o, s, v) -> {
//                    if (v == null)
//                        return "null";
//                    return v;
//                },
//                SerializerFeature.PrettyFormat,
//                SerializerFeature.SortField,
//                SerializerFeature.IgnoreNonFieldGetter,
//                SerializerFeature.WriteNullStringAsEmpty,
//                SerializerFeature.WriteNonStringKeyAsString,
//                SerializerFeature.WriteMapNullValue);
    }

    public static void main(String[] args) throws JsonProcessingException {

        log.info("------"+ObjectFormatter.toString(new T()));

    }

    @Getter
    @Accessors(fluent = true)
    public static class T {
        private String name;
    }
}
