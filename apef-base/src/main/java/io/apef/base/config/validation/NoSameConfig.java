package io.apef.base.config.validation;

import org.apache.commons.lang3.reflect.FieldUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.stream.Collectors;

public interface NoSameConfig {
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Constraint(validatedBy = {NotSameValidator.class})
    @interface NoSame {

        String message() default "Same values are not allowed";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

        Class type() default String.class;

        boolean includeNull() default false;
    }

    //Must be static class
    class NotSameValidator implements ConstraintValidator<NoSame, NoSameConfig> {
        private Class<?> type;
        private boolean includeNull;

        @Override
        public void initialize(NoSame constraintAnnotation) {
            this.type = constraintAnnotation.type();
            this.includeNull = constraintAnnotation.includeNull();
        }

        @Override
        public boolean isValid(NoSameConfig value, ConstraintValidatorContext context) {
            List<Object> values = FieldUtils.getAllFieldsList(value.getClass()).stream()
                    .filter(field -> this.type.isAssignableFrom(field.getType()))
                    .map(field -> {
                        try {
                            return FieldUtils.readField(field, value, true);
                        } catch (IllegalAccessException ignored) {
                        }
                        return null;
                    }).filter(o -> this.includeNull || o != null).collect(Collectors.toList());

            return values.size() == values.stream().distinct().count();
        }
    }
}
