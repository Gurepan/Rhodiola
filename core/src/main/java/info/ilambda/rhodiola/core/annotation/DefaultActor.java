package info.ilambda.rhodiola.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个默认的Actor，这个Actor只有在找不到其他Actor的时候才会尝试匹配，
 * 1.默认actor组名同发起者的组名一样
 * 2.要是默认actor名字存在，则必须和发起者的名字一样
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DefaultActor {
    String name() default "";
}
