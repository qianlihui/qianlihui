package com.qlh.base;

import java.util.Calendar;
import java.util.Date;

public class QlhDate {

    private Date date = new Date();

    public QlhDate() {

    }

    public QlhDate(String date, String format) {
        this.date = QlhDateUtils.parse(date, format);
    }

    public QlhDate(Date date) {
        this.date = date;
    }

    public int getHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public QlhDate setLastDateOfMonth() {
        trim(Calendar.MONTH);
        addMonth(1);
        addDay(-1);
        return this;
    }

    public QlhDate trim(int field) {
        date = QlhDateUtils.trim(date, field);
        return this;
    }

    public QlhDate addDay(int v) {
        date = QlhDateUtils.add(date, Calendar.DATE, v);
        return this;
    }

    public QlhDate addHour(int v) {
        date = QlhDateUtils.add(date, Calendar.HOUR, v);
        return this;
    }

    public QlhDate addMonth(int v) {
        date = QlhDateUtils.add(date, Calendar.MONTH, v);
        return this;
    }

    public Date toDate() {
        return date;
    }

    public String format(String format) {
        return QlhDateUtils.format(date, format);
    }

    public QlhDate clone() {
        return new QlhDate(date);
    }

    public boolean isSameDate(QlhDate d) {
        return QlhDateUtils.format(date, QlhDateUtils.FORMAT_DATE).equals(QlhDateUtils.format(d.date, QlhDateUtils.FORMAT_DATE));
    }

    public boolean isBeforeDate(QlhDate d) { // 按照日期比
        return format("yyyyMMdd").compareTo(d.format("yyyyMMdd")) < 0;
    }

    public boolean isAfterDate(QlhDate d) { // 按照日期比
        return format("yyyyMMdd").compareTo(d.format("yyyyMMdd")) > 0;
    }

    public boolean after(QlhDate d) {
        return date.after(d.date);
    }

    public boolean before(QlhDate d) {
        return date.before(d.date);
    }

    public static QlhDate now() {
        return new QlhDate();
    }

    public int get(int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(field);
    }
}
