package com.qlh.base;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    int len() default 32; // 字段长度

    String name() default ""; // 数据库字段名

    String wName() default ""; // 写入名称, 金蝶使用

    String comment() default ""; // 注释

    int scale() default 4; // 数字类型精度

    /**
     * 子元素名称, 用于集合/数组
     *
     * @return
     */
    String elementName() default "";

    boolean cassandra() default true;

}
