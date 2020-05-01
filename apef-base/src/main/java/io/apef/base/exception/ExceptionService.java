package io.apef.base.exception;

import io.apef.base.config.utils.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionService {
    private final static Logger log = LoggerFactory.getLogger(ExceptionService.class);
    private final static String DEFAULT_CONFIG = "vex-errors-config.xml";

    public static void loadExceptionConfiguration() {

        ExceptionsConfiguration config = ConfigurationLoader.loadConfiguration(
                ExceptionsConfiguration.class, DEFAULT_CONFIG);
        if (config == null) return;
        setApplicationExceptions("global", config.getGlobal());
        config.getApplications().forEach(ExceptionService::setApplicationExceptions);
    }

    public static void loadExceptionConfiguration(ErrorsConfig errorsConfig) {

        ExceptionsConfiguration config = ConfigurationLoader.loadConfiguration(
                ExceptionsConfiguration.class, DEFAULT_CONFIG, errorsConfig.getConfigFile());
        if (config == null) return;
        setApplicationExceptions("global", config.getGlobal());
        config.getApplications().forEach(ExceptionService::setApplicationExceptions);
    }

    public static void setApplicationExceptions(String appName, AppExCodeConfig config) {
        if (StringUtils.isEmpty(config.getClassName())) return;
        try {
            Class cls = Class.forName(config.getClassName());
            if (cls.isEnum()) {
                setEnumErrorCode(appName, cls, config);
            } else {
                setStaticErrorCode(appName, cls, config);
            }
        } catch (Exception ex) {
            log.warn("Failed to set Exception Configuration for application: " + appName, ex);
            return;
        }
    }

    private static void setEnumErrorCode(String appName, Class cls, AppExCodeConfig config) {
        if (!ErrorCode.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("The error code enum of application" +
                    appName + " must implement interface: " + ErrorCode.class);
        }
        config.getErrCodes().forEach((s, exCodeConfig) -> {
            Object item = Enum.valueOf(cls, s);
            try {
                FieldUtils.writeDeclaredField(item, "appCode", config.getAppCode(), true);
                FieldUtils.writeDeclaredField(item, "errCode", exCodeConfig.getErrCode(), true);
                FieldUtils.writeDeclaredField(item, "severity", exCodeConfig.getSeverity(), true);
                FieldUtils.writeDeclaredField(item, "message", exCodeConfig.getMessage(), true);
            } catch (Exception ex) {
                log.warn("Failed to configure the error code for " + item, ex);
            }
        });
    }

    private static void setStaticErrorCode(String appName, Class cls, AppExCodeConfig config) {
        config.getErrCodes().forEach((s, exCodeConfig) -> {
            try {
                Object item = FieldUtils.readStaticField(cls, s, true);
                if (!ErrorCode.class.isAssignableFrom(item.getClass())) {
                    log.warn("Failed to configure the error code for "
                            + s + " of application " + appName + ": must implement interface: " + ErrorCode.class);
                    return;
                }
                FieldUtils.writeDeclaredField(item, "appCode", config.getAppCode(), true);
                FieldUtils.writeDeclaredField(item, "errCode", exCodeConfig.getErrCode(), true);
                FieldUtils.writeDeclaredField(item, "severity", exCodeConfig.getSeverity(), true);
                FieldUtils.writeDeclaredField(item, "message", exCodeConfig.getMessage(), true);
            } catch (Exception ex) {
                log.warn("Failed to configure the error code for " + s + " of application " + appName, ex);
            }
        });
    }
}
