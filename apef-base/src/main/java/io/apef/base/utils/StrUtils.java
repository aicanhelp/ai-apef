package io.apef.base.utils;

public class StrUtils {
    public static int indexOf(final CharSequence cs, final int searchChar, int start, int end) {
        if (start < 0) {
            start = 0;
        }
        for (int i = start; i < end; i++) {
            if (cs.charAt(i) == searchChar) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(final String cs,
                              final String searchChar,
                              int start, int end) {
        if (end - start < searchChar.length()) return -1;
        int len = searchChar.length();
        if (end > cs.length()) end = cs.length();
        end = end - len + 1;
        for (int i = start; i < end; i++) {
            if (cs.regionMatches(i, searchChar, 0, len)) {
                return i;
            }
        }
        return -1;
    }
}
