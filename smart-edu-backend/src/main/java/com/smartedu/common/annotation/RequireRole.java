package com.smartedu.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义角色校验注解（AOP 切面）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许的角色代码：STUDENT, TEACHER, ACADEMIC, ADMIN, QB_ADMIN
     */
    String[] value() default {};
}
