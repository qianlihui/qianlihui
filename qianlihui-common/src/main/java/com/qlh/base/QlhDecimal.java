package com.qlh.base;

import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class QlhDecimal {

    public static BigDecimal trim(BigDecimal decimal) {
        return trim(decimal, 2);
    }

    public static BigDecimal trim(BigDecimal decimal, int scale) {
        return decimal.setScale(scale, RoundingMode.DOWN);
    }

    public static BigDecimal ifNull(BigDecimal v1, BigDecimal v2) {
        return v1 == null ? v2 : v1;
    }

    public static <T> T formatProperties(T obj, int scale, int... dept) {
        if (dept.length > 0 && dept[0] > 3) {
            return obj;
        }
        if (obj instanceof List) {
            ((List<?>) obj).forEach(object -> formatObj(object, scale, dept));
        } else if (obj.getClass().isArray()) {
            Object[] objects = (Object[]) obj;
            Lists.newArrayList(objects).forEach(object -> formatObj(object, scale, dept));
        } else {
            formatObj(obj, scale, dept);
        }
        return obj;
    }

    private static <T> T formatObj(T obj, int scale, int... dept) {
        if (dept.length > 0 && dept[0] > 3) {
            return obj;
        }

        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Object prop = field.get(obj);
                if (field.getType() == BigDecimal.class) {
                    field.set(obj, ((BigDecimal) prop).setScale(scale, RoundingMode.HALF_DOWN));
                } else if (!QlhClass.isPrimaryType(prop)) {
                    formatProperties(prop, scale, dept.length == 0 ? 1 : dept[0] + 1);
                }
                field.setAccessible(accessible);
            } catch (Exception e) {

            }
        }
        return obj;
    }
}
