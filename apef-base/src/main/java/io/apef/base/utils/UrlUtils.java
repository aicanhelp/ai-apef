package io.apef.base.utils;

import org.apache.commons.lang3.StringUtils;

public class UrlUtils {
    public static String concatUrl(String prefix, String suffix) {
        if (suffix == null) return prefix;
        if (suffix.startsWith("http")) return suffix;
        boolean prefixHasEnd = prefix.endsWith("/");
        boolean suffixHasStart = suffix.startsWith("/");
        if (prefixHasEnd) {
            if (!suffixHasStart) return prefix + suffix;
            else return prefix + StringUtils.replaceOnce(suffix, "/", "");
        }

        if (suffixHasStart) return prefix + suffix;
        return prefix + "/" + suffix;
    }

    public static String concatParams(String url, String params) {
        if (params == null) return url;
        int index = StringUtils.indexOf(url, '?');
        if (index == -1) return url + "?" + params;
        return url + "&" + params;
    }

    public static String resourcePath(String url) {
        return StringUtils.substringBeforeLast(url, "/");
    }
}
