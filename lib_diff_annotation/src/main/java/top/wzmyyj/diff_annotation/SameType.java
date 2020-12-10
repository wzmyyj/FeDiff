package top.wzmyyj.diff_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 2020/11/20.
 * <p>
 * 加上这个注解的属性，将用于 SameItem 或 SameContent 判断时穿透到内部属性。
 * 如果内部属性没有 {@link SameItem} 或 {@link SameContent} 或 {@link SameType} 标注，将没有作用。
 * 注意：不要产生穿透闭环。
 *
 * @author feling
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.FIELD) // 该注解作用在 字段上有作用
@Retention(RetentionPolicy.CLASS) // 要在编译时进行一些预处理操作，注解会在class文件中存在
public @interface SameType {

    // 用于填写表示这个字段的名称
    String value() default "";
}
