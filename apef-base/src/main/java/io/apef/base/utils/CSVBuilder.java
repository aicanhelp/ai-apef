package io.apef.base.utils;

public class CSVBuilder {
    private StringBuilder stringBuilder = new StringBuilder();
    private char separator = ',';

    public CSVBuilder() {
    }

    public CSVBuilder(char separator) {
        this.separator = separator;
    }

    public CSVBuilder append(String value) {
        stringBuilder.append(value == null ? "" : value)
                .append(this.separator);
        return this;
    }

    public CSVBuilder append(int value) {
        stringBuilder.append(value)
                .append(this.separator);
        return this;
    }

    public CSVBuilder append(long value) {
        stringBuilder.append(value).append(this.separator);
        return this;
    }

    public CSVBuilder append(boolean value) {
        stringBuilder.append(value).append(this.separator);
        return this;
    }

    public String toString() {
        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
