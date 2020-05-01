package io.apef.base.exception;

import io.apef.testing.unit.BaseUnitSpec;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ExceptionServiceTest extends BaseUnitSpec {

    @Test
    public void testLoadExceptionConfiguration() throws Exception {
        ExceptionService.loadExceptionConfiguration(new ErrorsConfig("exception-config.xml"));
        assertEquals(TestErrorCode.TIMEOUT.appCode(), "9999");
        assertEquals(TestErrorCode.TIMEOUT.errCode(), "900001");
        assertEquals(TestErrorCode.TIMEOUT.message(), "TT");
    }

    @Test
    public void testSetEnumTypeApplicationExceptions() throws Exception {
        AppExCodeConfig config = new AppExCodeConfig();
        config.setAppCode("888");
        config.setClassName(EnumType.class.getName());
        config.getErrCodes().put("Type1", new ExCodeConfig().setErrCode("20001").setMessage("Custom Type1"));
        ExceptionService.setApplicationExceptions("Test", config);
        log.info("" + EnumType.Type1.toString(""));
        assertEquals(EnumType.Type1.appCode(), "888");
        assertEquals(EnumType.Type1.errCode(), "20001");
        assertEquals(EnumType.Type1.message(), "Custom Type1");
    }

    @Test
    public void testSetStaticTypeApplicationExceptions() throws Exception {
        AppExCodeConfig config = new AppExCodeConfig();
        config.setAppCode("999");
        config.setClassName(StaticTypes.class.getName());
        config.getErrCodes().put("Type1", new ExCodeConfig().setErrCode("20001").setMessage("Custom static Type1"));
        ExceptionService.setApplicationExceptions("Test", config);
        assertEquals(StaticTypes.Type1.appCode(), "999");
        assertEquals(StaticTypes.Type1.errCode(), "20001");
        assertEquals(StaticTypes.Type1.message(), "Custom static Type1");
    }

    @Getter
    @Accessors(fluent = true)
    public enum EnumType implements ErrorCode {
        Type1("10001", ErrSeverity.MILD, "Enum Type1"),
        Type2("10002", ErrSeverity.MILD, "Enum Type2");

        private String appCode;
        private String errCode;
        private ErrSeverity severity;
        private String message;

        EnumType(String errorCode, ErrSeverity severity, String message) {
            this.errCode = errorCode;
            this.message = message;
            this.severity = severity;
        }
    }

    public static class StaticTypes {
        public final static StaticErrorCode Type1 = new StaticErrorCode(ErrSeverity.MILD, "0", "100001", "Static Type1");
        public final static StaticErrorCode Type2 = new StaticErrorCode(ErrSeverity.MILD, "0", "100002", "Static Type2");
    }

}