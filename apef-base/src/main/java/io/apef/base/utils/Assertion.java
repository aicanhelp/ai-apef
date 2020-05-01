package io.apef.base.utils;

import com.google.common.base.Preconditions;

public class Assertion {

    public static void assertArgumentEquals(Object anObject1, Object anObject2, String aMessage) {
        Preconditions.checkArgument(anObject1.equals(anObject2), aMessage);
    }

    public static void assertArgumentFalse(boolean aBoolean, String aMessage) {
        Preconditions.checkArgument(!aBoolean, aMessage);
    }

    public static void assertArgumentLength(String aString, int aMaximum, String aMessage) {
        int length = aString.trim().length();
        Preconditions.checkArgument(length <= aMaximum, aMessage);
    }

    public static void assertArgumentLength(String aString, int aMinimum, int aMaximum, String aMessage) {
        int length = aString.trim().length();
        Preconditions.checkArgument(aMinimum <= length && length <= aMaximum, aMessage);
    }

    public static void assertArgumentNotEmpty(String aString, String aMessage) {
        Preconditions.checkArgument(aString != null && !aMessage.trim().isEmpty(), aMessage);
    }

    public static void assertArgumentNotEquals(Object anObject1, Object anObject2, String aMessage) {
        Preconditions.checkArgument(!anObject1.equals(anObject2), aMessage);
    }

    public static void assertArgumentNotNull(Object anObject, String aMessage) {
        Preconditions.checkArgument(anObject != null, aMessage);
    }

    public static void assertArgumentRange(double aValue, double aMinimum, double aMaximum, String aMessage) {
        Preconditions.checkArgument(aMinimum <= aValue && aValue <= aMaximum, aMessage);
    }

    public static void assertArgumentRange(float aValue, float aMinimum, float aMaximum, String aMessage) {
        Preconditions.checkArgument(aMinimum <= aValue && aValue <= aMaximum, aMessage);
    }

    public static void assertArgumentRange(int aValue, int aMinimum, int aMaximum, String aMessage) {
        Preconditions.checkArgument(aMinimum <= aValue && aValue <= aMaximum, aMessage);
    }

    public static void assertArgumentRange(long aValue, long aMinimum, long aMaximum, String aMessage) {
        Preconditions.checkArgument(aMinimum <= aValue && aValue <= aMaximum, aMessage);
    }

    public static void assertArgumentTrue(boolean aBoolean, String aMessage) {
        Preconditions.checkArgument(aBoolean, aMessage);
    }

    public static void assertStateFalse(boolean aBoolean, String aMessage) {
        Preconditions.checkState(!aBoolean, aMessage);
    }

    public static void assertStateTrue(boolean aBoolean, String aMessage) {
        Preconditions.checkState(aBoolean, aMessage);
    }
}
