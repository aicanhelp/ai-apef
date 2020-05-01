package io.apef.base.config.utils;

import io.apef.testing.unit.BaseUnitSpec;
import io.apef.base.config.validation.ConfigValidator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class TestValidation extends BaseUnitSpec {
    @Data
    private static class Base {
        private boolean enabled;
    }

    private static class C1 extends Base {

    }


    private static abstract class CCCBase {
        @OneEnabled
        private List<Base> configs = new ArrayList<>();

        protected <T extends Base> T createConfig(T c) {
            this.configs.add(c);
            return c;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class CCC extends CCCBase {
        C1 c1 = this.createConfig(new C1());
        C1 c2 = this.createConfig(new C1());
        C1 c3 = this.createConfig(new C1());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Constraint(validatedBy = {OneEnabledValidator.class})
    public static @interface OneEnabled {

        String message() default "one and only one can be enabled";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

    }

    public static class OneEnabledValidator implements ConstraintValidator<OneEnabled, List<Base>> {


        @Override
        public void initialize(OneEnabled constraintAnnotation) {

        }

        @Override
        public boolean isValid(List<Base> value, ConstraintValidatorContext context) {
            return value.stream().filter(Base::isEnabled).count() == 1;
        }

    }

    @Test
    public void doValidate() {
        CCC ccc = new CCC();
        try {
            ConfigValidator.validate(ccc);
            Assert.fail();
        } catch (Exception ex) {

        }
        ccc.c1.setEnabled(true);
        ConfigValidator.validate(ccc);
        ccc.c2.setEnabled(true);
        try {
            ConfigValidator.validate(ccc);
            Assert.fail();
        } catch (Exception ex) {

        }
    }
}
