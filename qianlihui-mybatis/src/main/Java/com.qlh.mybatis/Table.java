package com.qlh.mybatis;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    String name() default "";

    String comment() default "";

    boolean updateOnDup() default false;

    String[] returnColumns() default {};

}
