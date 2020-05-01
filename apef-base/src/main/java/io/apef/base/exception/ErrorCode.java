package io.apef.base.exception;

public interface ErrorCode {
    ErrSeverity severity();

    String appCode();

    String errCode();

    String message();

    default String toString(String... additionMessages) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[App: ").append(appCode()).append("]")
                .append("[Code: ").append(this.errCode()).append("]")
                .append("[").append(this.severity()).append("] ")
                .append(this.message());
        if (additionMessages != null) {
            for (String message : additionMessages) {
                stringBuilder.append(" ").append(message);
            }
        }

        return stringBuilder.toString();
    }

    class General extends GeneralErrorCodes {
    }

}
