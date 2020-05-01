package io.apef.base.config.validation;

import io.apef.base.utils.ObjectFormatter;
import lombok.Data;
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

public abstract class OneEnabledConfig {
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Constraint(validatedBy = {OneEnabledValidator.class})
    public @interface OneEnabled {

        String message() default "one and only one can be enabled";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

    }

    //Must be static class
    public static class OneEnabledValidator implements ConstraintValidator<OneEnabled, Object> {

        @Override
        public void initialize(OneEnabled constraintAnnotation) {

        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            List<Object> values = FieldUtils.getAllFieldsList(value.getClass()).stream()
                    .filter(field ->
                            EnabledConfigBase.class.isAssignableFrom(field.getType()))
                    .map(field -> {
                        try {
                            return FieldUtils.readField(field, value, true);
                        } catch (IllegalAccessException ignored) {
                        }
                        return null;
                    }).collect(Collectors.toList());
            if (values.size() == 1) {
                ((EnabledConfigBase) values.get(0)).setEnabled(true);
                return true;
            }

            return values.stream().filter(o -> ((EnabledConfigBase) o).isEnabled()).count() == 1;
        }

    }

    @Data
    public static abstract class EnabledConfigBase {
        protected boolean enabled;

        @Override
        public String toString() {
            return ObjectFormatter.toString(this);
        }
    }
}
