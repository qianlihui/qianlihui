package com.qlh.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QlhDateUtils {

    public static final String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_TIME_CH = "yyyy年MM月dd日 HH时mm分ss秒";
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DATE_CH = "yyyy年MM月dd日";
    private static final ThreadLocal<Map<String, SimpleDateFormat>> localSimpleDateFormat = new ThreadLocal<>();

    public static int betweenDays(Date date1, Date date2) {
        date1 = trim(date1, Calendar.DATE);
        date2 = trim(date2, Calendar.DATE);

        if (date1.compareTo(date2) == 0)
            return 0;

        if (date1.after(date2)) {
            Date t = date1;
            date1 = date2;
            date2 = t;
        }

        int days = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        while (date1.before(date2)) {
            days++;
            calendar.add(Calendar.DATE, 1);
            date1 = calendar.getTime();
        }
        return days;
    }

    public static int betweenMinutes(Date date1, Date date2) {
        return (int) Math.abs(date1.getTime() - date2.getTime()) / 1000 / 60;
    }

    public static Date trim(Date date, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat simpleDateFormat;
        try {
            switch (field) {
                case Calendar.MINUTE:
                    simpleDateFormat = getSimpleDateFormat("yyyyMMddHHmm");
                    return simpleDateFormat.parse(simpleDateFormat.format(date));
                case Calendar.HOUR:
                    simpleDateFormat = getSimpleDateFormat("yyyyMMddHH");
                    return simpleDateFormat.parse(simpleDateFormat.format(date));
                case Calendar.DATE:
                    simpleDateFormat = getSimpleDateFormat(FORMAT_DATE);
                    return simpleDateFormat.parse(simpleDateFormat.format(date));
                case Calendar.MONTH:
                    simpleDateFormat = getSimpleDateFormat("yyyyMM");
                    return simpleDateFormat.parse(simpleDateFormat.format(date));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Not support to trim " + field);
    }

    public static Date parse(String dateString, String format) {
        try {
            return getSimpleDateFormat(format).parse(dateString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String format(Date date, String format) {
        try {
            return getSimpleDateFormat(format).format(date);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleDateFormat getSimpleDateFormat(String format) {
        Map<String, SimpleDateFormat> map = localSimpleDateFormat.get();
        if (map == null) {
            map = new HashMap<>();
            localSimpleDateFormat.set(map);
        }
        SimpleDateFormat simpleDateFormat = map.get(format);
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat(format);
            map.put(format, simpleDateFormat);
        }
        if (map.size() > 8) {
            map.clear();
        }
        return simpleDateFormat;
    }

    public static Date add(Date date, int field, int value) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, value);
        return calendar.getTime();
    }

    public static Date addDay(Date date, int value) {
        return add(date, Calendar.DATE, value);
    }

    public static Date addHour(Date date, int value) {
        return add(date, Calendar.HOUR, value);
    }

    public static Date addMinute(Date date, int value) {
        return add(date, Calendar.MINUTE, value);
    }

    public static Date addSecond(Date date, int value) {
        return add(date, Calendar.SECOND, value);
    }

    public static Date addYear(Date date, int value) {
        return add(date, Calendar.YEAR, value);
    }

    public static Date addDay(int value) {
        return addDay(now(), value);
    }

    public static Date trimDate(Date d) {
        return parse(format(d, FORMAT_DATE), FORMAT_DATE);
    }

    public static Date now() {
        return new Date();
    }

    public static Date getLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int lastDay = cal.getActualMaximum(Calendar.DATE);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        return cal.getTime();
    }
}
