package io.apef.base.config.validation;

import io.apef.base.utils.ObjectFormatter;
import org.hibernate.validator.HibernateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class ConfigValidator {
    protected final static Logger log = LoggerFactory.getLogger(ConfigValidator.class);
    private final static ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
            .configure()
            .buildValidatorFactory();
    private final static Validator validator = factory.getValidator();

    public static void validate(Object obj) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(obj);
        if (constraintViolations.size() > 0) {
            constraintViolations.forEach(violation ->
                            log.error(ObjectFormatter.toString(violation.getPropertyPath() + ":" + violation.getMessage()))
            );
            throw new IllegalArgumentException("Invalid Configuration: " + obj);
        }
    }
}
