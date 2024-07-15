package com.qlh.mybatis;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UniqueIndex {

    String[] value() default {};

    String comment() default "";

}
