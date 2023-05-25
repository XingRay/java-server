package com.xingray.java.server.spring.mvc.param;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonObjectField {
    String value() default "";
}
