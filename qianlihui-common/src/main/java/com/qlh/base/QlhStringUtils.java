package com.qlh.base;


import java.util.Collection;

public class QlhStringUtils {

    public static String ifBlank(String str, String str2) {
        return isNotBlank(str) ? str : str2;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String sub(String str, int start, int endExclude) {
        if (str == null)
            return null;
        if (start >= str.length())
            return "";
        return str.substring(start, str.length() >= endExclude ? endExclude : str.length());
    }

    public static String join(Object[] arr, String s) {
        StringBuilder builder = new StringBuilder();
        if (arr == null)
            return null;
        for (Object a : arr) {
            builder.append(a).append(s);
        }
        return builder.substring(0, builder.length() - s.length());
    }

    public static String join(Collection collection, String s) {
        StringBuilder builder = new StringBuilder();
        if (collection == null)
            return null;
        for (Object a : collection) {
            builder.append(a).append(s);
        }
        return builder.substring(0, builder.length() - s.length());
    }

    public static String trimAll(String s) {
        return s == null ? null : s.replaceAll("\\s+", "");
    }
}
