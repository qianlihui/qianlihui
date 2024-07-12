package com.qlh.base;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.regex.Pattern;

public class QlhValidations {

    public static final String RE_DATE = "^\\d{4}-\\d{2}-\\d{2}$"; // 日期格式
    public static final String RE_NUMBER = "^(-?\\d+)(\\.\\d+)?$"; // 数字
    public static final String RE_MOBILE = "^\\d{11}$";

    private static ThreadLocal<Boolean> expSwitch = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return Boolean.FALSE;
        }
    };

    private static final int ValidateErrorCode = 400;

    public static boolean checkTrue(boolean value, String msg, boolean... isThrow) {
        if (!value) {
            if (expSwitch.get() || (isThrow.length > 0 && isThrow[0]))
                throw new QlhException(ValidateErrorCode, msg);
            return false;
        }
        return true;
    }

    public static void checkTrue(boolean value, QlhException exp) {
        if (!value) {
            throw exp;
        }
    }

    public static boolean checkNotBlank(String value, String msg, boolean... isThrow) {
        if (StringUtils.isBlank(value)) {
            if ((isThrow.length > 0 && isThrow[0]) || expSwitch.get()) {
                throw new QlhException(ValidateErrorCode, msg);
            }
            return false;
        }
        return true;
    }

    public static void checkNotBlank(String value, QlhException exception) {
        if (StringUtils.isBlank(value)) {
            throw exception;
        }
    }

    public static boolean checkNotNull(Object value, String msg, boolean... isThrow) {
        if (value == null) {
            if (expSwitch.get() || (isThrow.length > 0 && isThrow[0]))
                throw new QlhException(ValidateErrorCode, msg);
            return false;
        }
        return true;
    }

    public static void checkNotNull(Object value, QlhException exception) {
        if (value == null) {
            throw exception;
        }
    }

    public static boolean checkRegex(Object value, String regex, String msg, boolean... isThrow) {
        if (value == null
                || QlhStringUtils.isBlank(value.toString())
                || !Pattern.matches(regex, value.toString())) {
            if (expSwitch.get() || (isThrow.length > 0 && isThrow[0]))
                throw new QlhException(ValidateErrorCode, msg);
            return false;
        }
        return true;
    }

    public static void checkRegex(Object value, String regex, QlhException exception) {
        if (value == null
                || QlhStringUtils.isBlank(value.toString())
                || !Pattern.matches(regex, value.toString())) {
            throw exception;
        }
    }

    public static boolean checkBetweenDate(Date s, Date e, String msg, boolean... isThrow) {
        if (s == null || e == null || s.after(e)) {
            if (expSwitch.get() || (isThrow.length > 0 && isThrow[0]))
                throw new QlhException(ValidateErrorCode, msg);
            return false;
        }
        return true;
    }

    public static void turnOnExpSwitch() {
        expSwitch.set(Boolean.TRUE);
    }

    public static void turnOffExpSwitch() {
        expSwitch.set(Boolean.FALSE);
    }

}
