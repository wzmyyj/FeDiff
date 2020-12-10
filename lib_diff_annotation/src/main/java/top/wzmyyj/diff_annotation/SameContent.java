package top.wzmyyj.diff_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 2020/11/20.
 *
 * 加上这个注解的属性，将用于 SameContent 的判断依据。
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.FIELD) // 该注解作用在 字段上有作用
@Retention(RetentionPolicy.CLASS) // 要在编译时进行一些预处理操作，注解会在class文件中存在
public @interface SameContent {

    // 用于填写表示这个字段的名称
    String value() default "";
}
